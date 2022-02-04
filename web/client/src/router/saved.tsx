import { RouteObject } from 'react-router-dom'
import Events from '../pages/local/Events'
import Stats from '../pages/local/Stats'
import GameView from '../pages/saved/Game'
import Games from '../pages/saved/Games'
import Login from '../pages/saved/Login'
import Player from '../pages/saved/Player'
import Players from '../pages/saved/Players'

const routes: RouteObject[] = [
   { path: '/', element: <Games /> },
   { path: '/login', element: <Login /> },
   { path: '/players', element: <Players /> },
   { path: '/players/:uuid', element: <Player /> },
   {
      path: '/game/:game',
      children: [
         { path: '', element: <GameView />, children: [{ path: 'player/:uuid', element: <Player /> }] },
         { path: 'events', element: <Events /> },
         { path: 'stats', element: <Stats /> },
      ],
   },
]

export default routes
