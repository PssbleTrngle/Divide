import { useMemo } from 'react'
import { Navigate, RouteObject, useRoutes } from 'react-router-dom'
import Orders from './components/Orders'
import Rewards from './components/Rewards'
import useSession from './hooks/useSession'
import useStatus from './hooks/useStatus'
import LoadingPage from './pages/LoadingPage'
import Events from './pages/local/Events'
import PlayerView from './pages/local/PlayerView'
import SpectatorView from './pages/local/SpectatorView'
import Stats from './pages/local/Stats'
import Logout from './pages/Logout'
import NotFound from './pages/NotFound'
import GameView from './pages/saved/Game'
import Games from './pages/saved/Games'
import Player from './pages/saved/Player'
import Players from './pages/saved/Players'

const commonRoutes: RouteObject[] = [{ path: '*', element: <NotFound /> }]

const localRoutes: RouteObject[] = [
   { path: '/events', element: <Events /> },
   { path: '/stats', element: <Stats /> },
   ...commonRoutes,
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
   ...localRoutes,
]

const spectatorRoutes: RouteObject[] = [
   { path: '/', element: <SpectatorView /> },
   { path: '/logout', element: <Navigate to='/' /> },
   ...localRoutes,
]

const savedRoutes: RouteObject[] = [
   { path: '/', element: <Games /> },
   { path: '/players', element: <Players /> },
   { path: '/players/:uuid', element: <Player /> },
   {
      path: '/game/:game',
      element: <GameView />,
      children: [
         { path: 'events', element: <Events /> },
         { path: 'stats', element: <Stats /> },
         { path: 'player/:uuid', element: <Player /> },
      ],
   },
   ...commonRoutes,
]

export default function useRouter() {
   const { loggedIn } = useSession()
   const { loading, type } = useStatus()

   const routes = useMemo(() => {
      switch (type) {
         case 'running':
            return loggedIn ? playerRoutes : spectatorRoutes
         case 'saved':
            return savedRoutes
         default:
            return commonRoutes
      }
   }, [loggedIn, type])

   const element = useRoutes(routes)
   if (loading) return <LoadingPage />
   return element
}
