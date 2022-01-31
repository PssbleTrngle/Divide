import { Router, Status } from "https://deno.land/x/oak/mod.ts"
import gameRouter from "./routes/game.ts"

const router = new Router()

router.get("/", ({ response }) => {
   response.body = { type: "saved" }
})
router.head("/", ({ response }) => (response.status = Status.OK))

router.use(async ({ response }, next) => {
   const before = Date.now()
   await next()
   const after = Date.now()
   response.headers.set("X-Response-Time", `${after - before}`)
})

router.use(async ({ state }, next) => {
   state.loggedIn = true
   state.isAdmin = true
   await next()
})

router.use("/game", gameRouter.routes(), gameRouter.allowedMethods())

export default router
