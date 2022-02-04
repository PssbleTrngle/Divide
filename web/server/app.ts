import { Application, Router, Status, colors } from "./deps.ts"
import apiRouter from "./router.ts"

const router = new Router()

router.use("/api", apiRouter.routes(), apiRouter.allowedMethods())

router.get("/mojang/:url*", async ({ response, params }) => {
   await fetch(`https://sessionserver.mojang.com/${params.url}`)
      .then(r => r.json())
      .then(data => (response.body = data))
      .catch(() => (response.status = Status.InternalServerError))
})

const app = new Application()

app.use(async ({ response }, next) => {
   try {
      await next()
   } catch (error) {
      console.error(error)
      response.status = error.status ?? Status.InternalServerError
      response.body = { message: error.message }
   }
})

app.use(router.routes())
app.use(router.allowedMethods())

app.addEventListener("listen", ({ hostname, port }) => {
   console.log(colors.bold("Listening on ") + colors.yellow(`${hostname}:${port}`))
})

await app.listen({ port: 8080 })
