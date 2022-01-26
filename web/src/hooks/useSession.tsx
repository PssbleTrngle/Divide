import { createContext, FC, useContext, useEffect, useReducer, useState } from 'react'
import { useQuery } from 'react-query'
import { useLocation, useNavigate } from 'react-router-dom'
import LoadingPage from '../pages/LoadingPage'
import LoggedOut from '../pages/LoggedOut'
import { request } from './useApi'


export interface Team {
   name: string
   id: string
   color?: number
}

export interface Player {
   name: string
   uuid: string
   team?: Team
}

export interface Session {
   token?: string
   player?: Player
   loggedIn: boolean
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

   const [isSpectator, joinSpectating] = useState(false)

   const [token, setToken] = useReducer((_: string | undefined, v: string | undefined) => {
      if (v) localStorage.setItem('token', v)
      else localStorage.removeItem('token')
      return v
   }, localStorage.getItem('token') ?? undefined)

   const { data: player } = useQuery('me', () => request<Player>('/api/auth', { token }), { enabled: !!token })

   useEffect(() => {
      const queryToken = new URLSearchParams(search).get('token')
      if (queryToken && token !== queryToken) {
         setToken(queryToken)
         navigate({ pathname })
      }
   }, [search, token])

   if (!token && !isSpectator) return <LoggedOut onSpectate={() => joinSpectating(true)} />
   if (token && !player) return <LoadingPage />

   return (
      <CTX.Provider value={{ token, player, loggedIn: !!player }}>
         {children}
      </CTX.Provider>
   )
}
