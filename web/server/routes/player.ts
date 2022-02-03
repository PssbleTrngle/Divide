import { Router, Status } from "../deps.ts"
import Games from "../models/games.ts"
import { Game } from "models/game.d.ts"
import { AggregatePipeline, Bson } from "../deps.ts"

const router = new Router()

function aggregate(params: { uuid?: string; game?: string } = {}) {
   const pipeline: AggregatePipeline<Game>[] = [{ $unwind: "$players" }]

   if (params.game) pipeline.push({ $match: { _id: new Bson.ObjectId(params.game) } })
   if (params.uuid) pipeline.push({ $match: { "players.uuid": params.uuid } })

   if (params.game)
      pipeline.push({ $unwind: "$events" }, { $match: { "events.type": "score" } }, { $sort: { "events.realTime": 1 } })

   const additionalInfo = params.game
      ? {
           team: { $first: "$players.team" },
           objectives: {
              $addToSet: "$events.event.objective",
           },
           scores: {
              $push: {
                 _id: "$events.id",
                 realTime: "$events.realTime",
                 gameTime: "$events.gameTime",
                 objective: "$events.event.objective",
                 score: "$events.event.score",
                 owner: "$events.event.player.uuid",
              },
           },
        }
      : {
           gamesCount: { $sum: 1 },
           games: {
              $push: {
                 _id: "$_id",
                 name: "$name",
                 startedAt: "$startedAt",
                 endedAt: "$endedAt",
                 uploadedAt: "$uploadedAt",
              },
           },
        }

   pipeline.push({
      $group: {
         _id: "$players.uuid",
         name: { $first: "$players.name" },
         uuid: { $first: "$players.uuid" },
         ...additionalInfo,
      },
   })

   const commonProps = ["team", "name", "uuid"]

   if (params.game)
      pipeline.push(
         { $unwind: "$objectives" },
         { $sort: { objectives: 1 } },
         {
            $project: {
               ...commonProps.reduce((o, key) => ({ ...o, [key]: 1 }), {}),
               scores: {
                  $filter: {
                     input: "$scores",
                     as: "score",
                     cond: {
                        $and: [{ $eq: ["$$score.objective", "$objectives"] }, { $eq: ["$$score.owner", "$uuid"] }],
                     },
                  },
               },
            },
         },
         {
            $group: {
               _id: "$uuid",
               ...commonProps.reduce((o, key) => ({ ...o, [key]: { $first: `$${key}` } }), {}),
               scores: {
                  $push: {
                     score: { $last: "$scores.score" },
                     objective: { $last: "$scores.objective" },
                     scores: "$scores",
                  },
               },
            },
         }
      )

   pipeline.push({ $sort: { gamesCount: -1,"team.name": 1, uuid: -1 } })

   return Games.aggregate(pipeline, { allowDiskUse: true }).toArray()
}

router.get("/", async ({ response }) => {
   response.body = await aggregate()
})

router.get("/:uuid", async ({ response, params }) => {
   const [player] = await aggregate(params)
   if (player) response.body = player
   else response.status = Status.NotFound
})

export default router
