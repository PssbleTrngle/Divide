import { RouteObject } from "react-router-dom";
import Events from "./pages/Events";
import Home from "./pages/Home";

const routes: RouteObject[] = [
   { path: '/', element: <Home /> },
   { path: '/events', element: <Events /> },
   { path: '*', element: <p>404 - Not Found</p> },
]

export default routes