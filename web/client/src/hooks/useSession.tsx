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
import { request, RequestError } from './useApi'
import useStatus from './useStatus'

export interface Session {
   token?: string
   player?: Player
   loggedIn?: boolean
   isAdmin?: boolean
   logout: DispatchWithoutAction
   login: Dispatch<string>
}

const CTX = createContext<Session | null>(null)

export default function useSession(): Session {
   const session = useContext(CTX)
   if (session) return session
   throw new Error('Session Provider missing')
}

function useMe<T>({ token, logout }: Pick<Session, 'token' | 'logout'>) {
   const { error, data } = useQuery<T, RequestError, T, string>('me', () => request<T>('/api/auth', { token }), {
      enabled: !!token,
   })

   useEffect(() => {
      if (token && error?.status === 401) logout()
   }, [error, token, logout])

   return token ? data : undefined
}

const RemoteSession: FC<Session> = ({ children, ...session }) => {
   const data = useMe<Partial<Session>>(session)
   return <CTX.Provider value={{ ...session, ...data, loggedIn: !!data }}>{children}</CTX.Provider>
}

const LocalSession: FC<Session> = ({ children, ...session }) => {
   const [isSpectator, joinSpectating] = useState(false)
   const { search, pathname } = useLocation()

   const player = useMe<Player>(session)

   useEffect(() => {
      const queryToken = new URLSearchParams(search).get('token')
      if (queryToken && session.token !== queryToken) {
         session.login(queryToken)
      }
   }, [pathname, search, session])

   if (!session.token && !isSpectator) return <LoggedOut onSpectate={() => joinSpectating(true)} />
   if (session.token && !player) return <LoadingPage />

   return <CTX.Provider value={{ ...session, player, loggedIn: !!player }}>{children}</CTX.Provider>
}

const EmptySession: FC = ({ children }) => (
   <CTX.Provider value={{ login: () => {}, logout: () => {} }}>{children}</CTX.Provider>
)

export const SessionProvider: FC = ({ children }) => {
   const client = useQueryClient()
   const { type, loading } = useStatus()
   const navigate = useNavigate()
   const { search } = useLocation()

   const [token, setToken] = useReducer((_: string | undefined, v: string | undefined) => {
      if (v) localStorage.setItem('token', v)
      else localStorage.removeItem('token')
      return v
   }, localStorage.getItem('token') ?? undefined)

   const login = useCallback(
      (token?: string) => {
         setToken(token)
         client.setQueryData('me', undefined)
         client.invalidateQueries({ predicate: () => true })
         const redirect = new URLSearchParams(search).get('redirect') ?? '/'
         navigate(redirect)
      },
      [setToken, navigate, client, search]
   )

   const logout = useCallback(() => {
      login(undefined)
   }, [login])

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
