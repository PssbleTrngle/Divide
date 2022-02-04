import { RouteObject } from 'react-router-dom'
import SpectatorView from '../pages/local/SpectatorView'
import localRoutes from './local'
import loggedOutRoutes from './loggedOut'

const routes: RouteObject[] = [{ path: '/', element: <SpectatorView /> }, ...localRoutes, ...loggedOutRoutes]

export default routes
