import { Navigate, RouteObject } from 'react-router-dom'
import Orders from '../components/Orders'
import Rewards from '../components/Rewards'
import PlayerView from '../pages/local/PlayerView'
import localRoutes from './local'
import loggedInRoutes from './loggedIn'

const routes: RouteObject[] = [
   {
      path: '/',
      element: <PlayerView />,
      children: [
         { path: 'rewards', element: <Rewards /> },
         { path: 'orders', element: <Orders /> },
         { path: '', element: <Navigate to='rewards' /> },
      ],
   },
   ...loggedInRoutes,
   ...localRoutes,
]

export default routes
