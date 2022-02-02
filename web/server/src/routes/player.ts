import { Router, Status } from "oak"
import Games from "../models/games.ts"
import { Game } from "models/game.d.ts"
import { isAdmin } from "../middleware/permissions.ts"
import { Event, EventType, ScoreEvent } from "models/events.d.ts"
import { exists } from "../util.ts"
import { AggregatePipeline } from "mongo"
import { Player } from "models/player.d.ts"

const router = new Router()

const pipeline: AggregatePipeline<Game>[] = [
   { $unwind: "$players" },
   {
      $group: {
         _id: "$players.uuid",
         name: { $first: "$players.name" },
         uuid: { $first: "$players.uuid" },
         games: {
            $push: {
               _id: "$_id",
               name: "$name",
               startedAt: "$startedAt",
               endedAt: "$endedAt",
               uploadedAt: "$uploadedAt",
            },
         },
         gamesCount: { $sum: 1 },
      },
   },
   { $sort: { gamesCount: -1, uuid: 1 } },
]

router.get("/", async ({ response }) => {
   response.body = await Games.aggregate(pipeline).toArray()
})

router.get("/:id", async ({ response, params }) => {
   const [player] = await Games.aggregate([...pipeline, { $match: { uuid: params.id } }]).toArray()
   if (player) response.body = player
   else response.status = Status.NotFound
})

export default router
