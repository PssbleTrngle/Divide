import { createContext, FC, useContext, useEffect, useReducer } from 'react'
import { useQuery } from 'react-query'
import { useLocation, useNavigate } from 'react-router-dom'
import Banner from '../components/Banner'
import LoadingPage from '../pages/LoadingPage'
import LoggedOut from '../pages/LoggedOut'
import { request } from './useApi'

export interface Player {
   name: string
   uuid: string
   team?: string
}

export interface Session {
   token: string
   player: Player
}

const CTX = createContext<Session | null>(null)

export default function useSession(): Session {
   const session = useContext(CTX)
   if (session) return session
   throw new Error('Session Provider missing')
}

export const SessionProvider: FC = ({ children }) => {
   const { search, pathname } = useLocation()
   const navigate = useNavigate()

   const [token, setToken] = useReducer((_: string | undefined, v: string | undefined) => {
      if (v) localStorage.setItem('token', v)
      else localStorage.removeItem('token')
      return v
   }, localStorage.getItem('token') ?? undefined)

   const { data: player, error } = useQuery('me', () => request<Player>('/api/auth', { token }), { enabled: !!token })

   useEffect(() => {
      const queryToken = new URLSearchParams(search).get('token')
      if (queryToken && token !== queryToken) {
         setToken(queryToken)
         navigate({ pathname })
      }
   }, [search, token])

   if (!token) return <LoggedOut />
   if (!player) return <LoadingPage />
   return (
      <CTX.Provider value={{ token, player }}>
         {error && <Banner>Offline</Banner>}
         {children}
      </CTX.Provider>
   )
}
