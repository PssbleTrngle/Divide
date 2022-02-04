import { useMemo } from 'react'
import { RouteObject, useRoutes } from 'react-router-dom'
import useSession from '../hooks/useSession'
import useStatus from '../hooks/useStatus'
import Forbidden from '../pages/Forbidden'
import LoadingPage from '../pages/LoadingPage'
import NotFound from '../pages/NotFound'
import adminRoutes from './admin'
import loggedInRoutes from './loggedIn'
import loggedOutRoutes from './loggedOut'
import playerRoutes from './player'
import savedRoutes from './saved'
import spectatorRoutes from './spectator'

const commonRoutes: RouteObject[] = [{ path: '*', element: <NotFound /> }]

export default function useRouter() {
   const { loggedIn, isAdmin } = useSession()
   const { loading, type } = useStatus()

   const routes = useMemo(() => {
      const routes = [...commonRoutes]

      switch (type) {
         case 'running':
            routes.push(...(loggedIn ? playerRoutes : spectatorRoutes))
            break
         case 'saved':
            routes.push(...savedRoutes)
            routes.push(...(loggedIn ? loggedInRoutes : loggedOutRoutes))
            break
      }

      if (isAdmin) routes.push(...adminRoutes)
      else routes.push(...adminRoutes.map(r => ({ ...r, element: <Forbidden /> })))

      return routes
   }, [loggedIn, isAdmin, type])

   const element = useRoutes(routes)
   if (loading) return <LoadingPage />
   return element
}
