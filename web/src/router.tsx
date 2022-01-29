import { useMemo } from 'react'
import { Navigate, RouteObject, useRoutes } from 'react-router-dom'
import Orders from './components/Orders'
import Rewards from './components/Rewards'
import useSession from './hooks/useSession'
import Events from './pages/Events'
import Logout from './pages/Logout'
import NotFound from './pages/NotFound'
import PlayerView from './pages/PlayerView'
import SpectatorView from './pages/SpectatorView'
import Stats from './pages/Stats'

const commonRoutes: RouteObject[] = [
   { path: '*', element: <NotFound /> },
   { path: '/events', element: <Events /> },
   { path: '/stats', element: <Stats /> },
]

const playerRoutes: RouteObject[] = [
   {
      path: '/',
      element: <PlayerView />,
      children: [
         { path: 'rewards', element: <Rewards /> },
         { path: 'orders', element: <Orders /> },
         { path: '', element: <Navigate to='rewards' /> },
      ],
   },
   { path: '/logout', element: <Logout /> },
   ...commonRoutes,
]

const spectatorRoutes: RouteObject[] = [
   { path: '/', element: <SpectatorView /> },
   { path: '/logout', element: <Navigate to='/' /> },
   ...commonRoutes,
]

export default function useRouter() {
   const { loggedIn } = useSession()
   const routes = useMemo(() => (loggedIn ? playerRoutes : spectatorRoutes), [loggedIn])
   return useRoutes(routes)
}
