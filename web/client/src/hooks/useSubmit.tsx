import { SyntheticEvent, useCallback, useMemo, useState } from 'react'
import { useQueryClient } from 'react-query'
import { request, RequestError } from './useApi'
import useSession from './useSession'

type Body = Record<string, unknown> | FormData

function isEvent(e?: Body | SyntheticEvent): e is SyntheticEvent {
   return !!e && 'preventDefault' in e && typeof (e as SyntheticEvent).preventDefault === 'function'
}

export default function useSubmit<T>(
   uri: string,
   { data, keys, method = 'POST', body, ...config }: RequestInit & { data?: Body; keys?: string[] } = {}
) {
   const { token } = useSession()
   const [error, setError] = useState<RequestError>()
   const [response, setResponse] = useState<T>()
   const client = useQueryClient()
   const send = useCallback(
      async (e?: Body | SyntheticEvent, reThrow = false) => {
         if (isEvent(e)) e.preventDefault()

         const callbackData = isEvent(e) ? undefined : e
         setResponse(undefined)
         setError(undefined)

         try {
            const response = await request<T>(`/api/${uri}`, {
               ...config,
               method,
               token,
               body: body ?? JSON.stringify(callbackData ?? data),
            })
            setResponse(response)
            client.invalidateQueries(uri)
            keys?.forEach(key => client.invalidateQueries(key))
         } catch (e) {
            setError(e as RequestError)
            if (reThrow) throw e
         }
      },
      [data, uri, config, method, token, keys, client, setError, setResponse, body]
   )

   return useMemo(() => ({ send, error, data: response }), [send, error, response])
}

export type Submit = ReturnType<typeof useSubmit>
