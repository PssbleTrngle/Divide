import Games from "../models/games.ts"
import { Game } from "models/game.d.ts"
import { Bson, AggregatePipeline, Router, Status } from "../deps.ts"

const router = new Router()

function pipeline(params: { game?: string; type?: string } = {}) {
   const pipeline: AggregatePipeline<Game>[] = []
   if (params.game) pipeline.push({ $match: { _id: new Bson.ObjectId(params.game) } })
   pipeline.push({ $unwind: "$events" })
   if (params.type) pipeline.push({ $match: { "events.type": params.type } })
   pipeline.push({ $group: { _id: "$_id", events: { $push: "$events" } } })
   return pipeline
}

router.get("/", async ({ response, params }) => {
   const [game] = await Games.aggregate(pipeline(params)).toArray()
   if (game) response.body = game.events
   else response.status = Status.NotFound
})

router.get("/:type", async ({ response, params }) => {
   const [game] = await Games.aggregate(pipeline(params)).toArray()
   if (game) response.body = game.events
   else response.status = Status.NotFound
})

export default router
