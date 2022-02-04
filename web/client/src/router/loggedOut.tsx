import { Navigate, RouteObject } from 'react-router-dom'

const routes: RouteObject[] = [{ path: '/logout', element: <Navigate to='/' /> }]

export default routes
