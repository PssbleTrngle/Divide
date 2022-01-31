import { Router, Status } from "oak"
import Games from "../models/games.ts"
import { Game } from "models/game.d.ts"
import { isAdmin } from "../middleware/permissions.ts"
import { Event, EventType } from "models/events.d.ts"
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
      { $limit: 100 },
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
            .split("\n")
            .map(e => e.trim())
            .filter(e => e.length)
            .map(e => JSON.parse(e) as Event)
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
