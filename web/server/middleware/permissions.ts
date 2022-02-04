import { RouterMiddleware, RouteParams, Status } from "../deps.ts"
import { SessionState } from "./authenticate.ts"

export function isAdmin<R extends string>(): RouterMiddleware<R, RouteParams<R>, SessionState> {
   return async (ctx, next) => {
      if (ctx.state.session?.isAdmin) await next()
      else ctx.throw(Status.Unauthorized)
   }
}

export function isUser<R extends string>(): RouterMiddleware<R, RouteParams<R>, SessionState> {
   return async (ctx, next) => {
      if (ctx.state.session) await next()
      else ctx.throw(Status.Unauthorized)
   }
}
