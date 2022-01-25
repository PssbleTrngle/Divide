import { useCallback, useMemo, useState } from 'react'
import { request, RequestError } from './useApi'
import useSession from './useSession'

export default function useSubmit(
   uri: string,
   { data, ...config }: Omit<RequestInit, 'body'> & { data?: Record<string, unknown> } = {}
) {
   const { token } = useSession()
   const [error, setError] = useState<RequestError>()
   const body = useMemo(() => JSON.stringify(data), [data])
   const send = useCallback(async () => {
      setError(undefined)
      try {
         await request(`/api/${uri}`, { ...config, token, body })
      } catch (e) {
         setError(e as RequestError)
      }
   }, [uri, body])
   return { send, error }
}
