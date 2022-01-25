import { SyntheticEvent, useCallback, useMemo, useState } from 'react'
import { useQueryClient } from 'react-query'
import { request, RequestError } from './useApi'
import useSession from './useSession'

type Body = Record<string, unknown>

function isEvent(e?: any): e is SyntheticEvent {
   return !!e && 'preventDefault' in e && typeof (e as SyntheticEvent).preventDefault === 'function'
}

export default function useSubmit(
   uri: string,
   { data, keys, ...config }: Omit<RequestInit, 'body'> & { data?: Body; keys?: string[] } = {}
) {
   const { token } = useSession()
   const [error, setError] = useState<RequestError>()
   const client = useQueryClient()
   const send = useCallback(
      async (e?: Body | SyntheticEvent, reThrow = false) => {
         if (isEvent(e)) e.preventDefault()
         const callbackData = isEvent(e) ? undefined : e
         const body = JSON.stringify(callbackData ?? data)
         setError(undefined)
         try {
            await request(`/api/${uri}`, { ...config, token, body })
            keys?.forEach(key => client.invalidateQueries(key))
         } catch (e) {
            setError(e as RequestError)
            if (reThrow) throw e
         }
      },
      [uri, data]
   )
   return useMemo(() => ({ send, error }), [send, error])
}

export type Submit = ReturnType<typeof useSubmit>
