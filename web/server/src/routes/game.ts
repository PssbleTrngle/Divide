import { Router, Status } from "oak"
import Games from "../models/games.ts"
import { Game } from "models/game.d.ts"
import { isAdmin } from "../middleware/permissions.ts"
import { Event, EventType, ScoreEvent } from "models/events.d.ts"
import { exists } from "../util.ts"
import { Bson } from "mongo"

const router = new Router()

const projection = { events: 0 }

router.get("/", async ({ response }) => {
   response.body = await Games.find({}, { projection }).toArray()
})

router.get("/:id", async ({ response, params }) => {
   const game = await Games.findOne({ _id: new Bson.ObjectId(params.id) }, { projection })
   if (game) response.body = game
   else response.status = Status.NotFound
})

router.get("/:id/events", async ({ response, params }) => {
   const game = await Games.findOne({ _id: new Bson.ObjectId(params.id) }, { projection: { events: 1 } })
   if (game) response.body = game.events
   else response.status = Status.NotFound
})

router.get("/:id/events/:type", async ({ response, params }) => {
   const [game] = await Games.aggregate<Game>([
      { $match: { _id: new Bson.ObjectId(params.id) } },
      { $unwind: "$events" },
      { $match: { "events.type": params.type } },
      { $group: { _id: "$_id", events: { $push: "$events" } } },
   ]).toArray()

   if (game) response.body = game.events
   else response.status = Status.NotFound
})

router.post("/", isAdmin(), async ctx => {
   const body = ctx.request.body({ type: "form-data" })
   const { files } = await body.value.read()

   if (!files?.length) return ctx.throw(Status.BadRequest, "files missing")

   const events = await Promise.all(
      files.map(async ({ filename }) => {
         if (!filename) return []
         const content = await Deno.readTextFile(filename)

         const events = content
            .replaceAll("613891f0-96f5-3dc4-9a30-ea9a70f91dd4", "3a5af8e5-b97c-4c1e-a219-ee5383942ae3")
            .replaceAll("23c87f5c-80d7-3c48-875d-631802f24527", "061ed725-1287-4e9a-ad0a-62a1a8706e58")
            .replaceAll("5f7159c8-d481-397f-990d-35725c863624", "76be3424-87c5-4df9-8cd2-175e0784beaa")
            .replaceAll("68a87e5f-47d3-3828-aef7-cd2e35ee564c", "3bc28cab-04f8-41fc-b343-4060cb515f44")
            .replaceAll("c89335d3-abbc-3bbe-aa80-f98c6904fa76", "bd2dafb0-44b0-4458-b4d9-fd4121ef6344")
            .split("\n")
            .map(e => e.trim())
            .filter(e => e.length)
            .map(e => JSON.parse(e) as Event)
            .filter(e => e.type !== "score" || (e.event as ScoreEvent).objective !== "Jumps")
            .sort((a, b) => a.realTime - b.realTime)

         await Deno.remove(filename)
         return events
      })
   ).then(a => a.flat())

   if (!events.length) return ctx.throw(Status.BadRequest, "no events")

   console.log(`Loaded ${events.length} events`)

   function isEvent<T extends EventType>(type: T) {
      return (event: Event): event is Event<T> => event.type === type
   }

   const gameEvents = events.filter(isEvent("game"))
   const startedAt = gameEvents.find(e => e.event.action === "started")?.realTime ?? events[0].realTime
   const endedAt = gameEvents.find(e => e.event.action === "ended")?.realTime ?? events[events.length - 1].realTime

   const players = events
      .filter(isEvent("death"))
      .map(it => [it.event.player, it.event.killer])
      .flat()
      .filter(exists)
      .filter((p1, i1, a) => !a.some((p2, i2) => i2 < i1 && p1.uuid === p2.uuid))

   await Games.insertOne({
      startedAt: new Date(startedAt),
      endedAt: new Date(endedAt),
      uploadedAt: new Date(),
      events,
      players,
   })

   ctx.response.status = Status.Created
})

export default router
