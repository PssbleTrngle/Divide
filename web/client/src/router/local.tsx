import { RouteObject } from "react-router-dom"
import Events from "../pages/local/Events"
import Stats from "../pages/local/Stats"

const routes: RouteObject[] = [
   { path: '/events', element: <Events /> },
   { path: '/stats', element: <Stats /> },
]

export default routes
