import { RouterMiddleware, RouteParams, Status } from "oak"

interface SessionState {
   loggedIn: boolean
   isAdmin: boolean
}

export function isAdmin<R extends string>(): RouterMiddleware<R, RouteParams<R>, SessionState> {
   return async (ctx, next) => {
      if (ctx.state.isAdmin && ctx.state.loggedIn) await next()
      else ctx.throw(Status.Unauthorized)
   }
}
