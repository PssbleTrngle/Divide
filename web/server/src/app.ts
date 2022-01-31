import { Application, Router } from "oak"
import apiRouter from "./router.ts"
import { bold, yellow } from "fmt/colors.ts"

const router = new Router()

router.use("/api", apiRouter.routes(), apiRouter.allowedMethods())

const app = new Application()
app.use(router.routes())
app.use(router.allowedMethods())

app.addEventListener("listen", ({ hostname, port }) => {
   console.log(bold("Listening on ") + yellow(`${hostname}:${port}`))
})

await app.listen({ port: 8080 })
