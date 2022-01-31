import { useEffect, VFC } from 'react'
import useSession from '../hooks/useSession'
import LoadingPage from './LoadingPage'

const Logout: VFC = () => {
   const session = useSession()
   useEffect(() => {
      if (session.token) session.logout()
      console.log('logging out')
   }, [session.token])
   return <LoadingPage />
}

export default Logout
