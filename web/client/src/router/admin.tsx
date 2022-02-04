import { RouteObject } from 'react-router-dom'
import Edit from '../pages/saved/Edit'
import Upload from '../pages/saved/Upload'

const routes: RouteObject[] = [
   { path: '/game/upload', element: <Upload /> },
   { path: '/game/:game/edit', element: <Edit /> },
]

export default routes