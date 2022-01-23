import { useCallback } from 'react'
import { request } from './useApi'
import useSession from './useSession'

export default function useSubmit(uri: string, config?: RequestInit) {
   const { token } = useSession()
   return useCallback(() => request(`/api/${uri}`, { ...config, token }).catch(console.error), [uri])
}
