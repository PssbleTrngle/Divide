import { RouterMiddleware, RouteParams, Status, jwt } from "../deps.ts"
import config from "../config.ts"

export interface TokenPayload extends jwt.Payload {
   exp: number
}

export interface SessionState {
   session?: {
      isAdmin: boolean
   }
}

export default function authenticate<R extends string>(): RouterMiddleware<R, RouteParams<R>, SessionState> {
   return async (ctx, next) => {
      const header = ctx.request.headers.get("Authorization")

      if (header) {
         const [type, token] = header.split(" ")
         if (type !== "Bearer" && type !== "Token")
            return ctx.throw(Status.BadRequest, `invalid authentication type '${type}'`)

         try {
            const payload = (await jwt.verify(token, config.jwt.key)) as TokenPayload
            if (payload.exp <= Date.now()) return ctx.throw(Status.Unauthorized, "token expired")

            ctx.state.session = {
               isAdmin: true,
            }

         } catch {
            return ctx.throw(Status.Unauthorized, "invalid token")
         }
      }

      await next()
   }
}
