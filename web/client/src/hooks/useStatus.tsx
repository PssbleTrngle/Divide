import { createContext, FC, useContext, useMemo } from 'react'
import { useQuery } from 'react-query'
import Banner from '../components/Banner'
import { Status } from '../models/api'
import { request } from './useApi'

const CTX = createContext<Status | null>(null)

export default function useStatus() {
   const status = useContext(CTX)
   if (status) return status
   throw new Error('StatusProvider missing')
}

export const StatusProvider: FC = ({ children }) => {
   const { data, isSuccess } = useQuery('api-status', () => request<Status>('/api'))
   const status = useMemo(() => ({ ...data, loading: !data } as Status), [data])
   
   return (
      <CTX.Provider value={status}>
         {isSuccess || <Banner>Server Offline</Banner>}
         {children}
      </CTX.Provider>
   )
}
