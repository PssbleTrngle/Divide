import {
   createContext,
   Dispatch,
   DispatchWithoutAction,
   FC,
   useCallback,
   useContext,
   useEffect,
   useMemo,
   useReducer,
   useState
} from 'react'
import { useQuery, useQueryClient } from 'react-query'
import { useLocation, useNavigate } from 'react-router-dom'
import { Player } from '../models/player'
import LoadingPage from '../pages/LoadingPage'
import LoggedOut from '../pages/LoggedOut'
import { request } from './useApi'
import useStatus from './useStatus'

export interface Session {
   token?: string
   player?: Player
   loggedIn?: boolean
   logout: DispatchWithoutAction
   login: Dispatch<string>
}

const CTX = createContext<Session | null>(null)

export default function useSession(): Session {
   const session = useContext(CTX)
   if (session) return session
   throw new Error('Session Provider missing')
}

const RemoteSession: FC<Session> = ({ children, ...session }) => {
   return <CTX.Provider value={session}>{children}</CTX.Provider>
}

const LocalSession: FC<Session> = ({ children, token, ...session }) => {
   const [isSpectator, joinSpectating] = useState(false)
   const { search, pathname } = useLocation()
   const navigate = useNavigate()

   const { data: player } = useQuery('me', () => request<Player>('/api/auth'), { enabled: !!token })

   useEffect(() => {
      const queryToken = new URLSearchParams(search).get('token')
      if (queryToken && token !== queryToken) {
         session.login(queryToken)
         navigate({ pathname })
      }
   }, [navigate, pathname, search, session, token])

   if (!token && !isSpectator) return <LoggedOut onSpectate={() => joinSpectating(true)} />
   if (token && !player) return <LoadingPage />

   return <CTX.Provider value={{ ...session, token, player, loggedIn: !!player }}>{children}</CTX.Provider>
}

const EmptySession: FC = ({ children }) => (
   <CTX.Provider value={{ login: () => {}, logout: () => {} }}>{children}</CTX.Provider>
)

export const SessionProvider: FC = ({ children }) => {
   const client = useQueryClient()
   const { type, loading } = useStatus()
   const navigate = useNavigate()

   const [token, login] = useReducer((_: string | undefined, v: string | undefined) => {
      if (v) localStorage.setItem('token', v)
      else localStorage.removeItem('token')
      return v
   }, localStorage.getItem('token') ?? undefined)

   const logout = useCallback(() => {
      login(undefined)
      navigate('/')
      client.invalidateQueries({ predicate: () => true })
   }, [client, navigate])

   const session = useMemo(() => ({ token, login, logout }), [token, login, logout])

   return useMemo(() => {
      if (loading) return <LoadingPage />
      switch (type) {
         case 'running':
            return <LocalSession {...session}>{children}</LocalSession>
         case 'saved':
            return <RemoteSession {...session}>{children}</RemoteSession>
         default:
            return <EmptySession />
      }
   }, [loading, type, session, children])
}
