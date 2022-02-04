import { Router, Status, jwt } from "../deps.ts"
import config from "../config.ts"
import { TokenPayload } from "../middleware/authenticate.ts"
import { isUser } from "../middleware/permissions.ts"

const router = new Router()

router.get("/", isUser(), ({ state, response }) => {
   response.body = state.session
})

router.post("/", async ctx => {
   const { password } = await ctx.request.body({ type: "json" }).value

   if (password !== config.password) ctx.throw(Status.BadRequest, "invalid password")

   const payload: TokenPayload = { exp: Date.now() + config.jwt.expiresIn }
   const token = await jwt.create({ alg: "HS512", typ: "JWT" }, payload, config.jwt.key)

   ctx.response.body = { token }
})

export default router
