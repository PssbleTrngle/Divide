import { useMemo } from 'react'
import { RouteObject, useRoutes } from 'react-router-dom'
import useSession from './hooks/useSession'
import Events from './pages/Events'
import NotFound from './pages/NotFound'
import PlayerView from './pages/PlayerView'
import SpectatorView from './pages/SpectatorView'

const commonRoutes: RouteObject[] = [
   { path: '*', element: <NotFound /> },
   { path: '/events', element: <Events /> },
]

const playerRoutes: RouteObject[] = [{ path: '/', element: <PlayerView /> }, ...commonRoutes]

const spectatorRoutes: RouteObject[] = [{ path: '/', element: <SpectatorView /> }, ...commonRoutes]

export default function useRouter() {
   const { loggedIn } = useSession()
   const routes = useMemo(() => (loggedIn ? playerRoutes : spectatorRoutes), [loggedIn])
   return useRoutes(routes)
}
