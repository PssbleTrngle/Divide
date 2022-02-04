import { Router, Status } from "https://deno.land/x/oak/mod.ts"
import gameRouter from "./routes/game.ts"
import playerRouter from "./routes/player.ts"
import authRouter from "./routes/auth.ts"
import authenticate from "./middleware/authenticate.ts"

const router = new Router()

router.use(async ({ response }, next) => {
   const before = Date.now()
   await next()
   const after = Date.now()
   response.headers.set("X-Response-Time", `${after - before}`)
})

router.use(authenticate())

router.get("/", ({ response }) => {
   response.body = { type: "saved" }
})

router.head("/", ({ response }) => (response.status = Status.OK))

router.use("/game", gameRouter.routes(), gameRouter.allowedMethods())
router.use("/player", playerRouter.routes(), playerRouter.allowedMethods())
router.use("/auth", authRouter.routes(), authRouter.allowedMethods())

export default router
