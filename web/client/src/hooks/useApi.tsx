import { useMemo, useState } from 'react'
import { useQuery, UseQueryOptions, UseQueryResult } from 'react-query'
import useSession from './useSession'

export class RequestError extends Error {
   constructor(public readonly status: number, message: string, public readonly stacktrace?: string[]) {
      super(message)
   }
}

export async function request<T>(url: string, { token, ...config }: Partial<RequestInit> & { token?: string } = {}) {
   const response = await fetch(url.endsWith('/') ? url : `${url}/`, {
      ...config,
      headers: {
         Authorization: token ? `Bearer ${token}` : '',
         ...(typeof config.body === 'string'
            ? {
                 'Content-Type': 'application/json',
              }
            : {}),
         ...config?.headers,
      },
   })

   const json = await response.json().catch(() => null)

   if (!response.ok) throw new RequestError(response.status, json?.message ?? response.statusText, json?.stack)

   return json as T
}

export type ApiConfig<T> = Omit<UseQueryOptions<T, RequestError, T, string>, 'queryKey' | 'queryFn'>

export default function useApi<T>(uri: string, config?: ApiConfig<T>) {
   const { token } = useSession()
   const [error, setError] = useState<RequestError | null>(null)
   const query = useQuery<T, RequestError, T, string>(uri, () => request<T>(`/api/${uri}`, { token }), {
      ...config,
      onError: setError,
      onSuccess: () => setError(null),
   })
   return useMemo(() => ({ ...query, error } as UseQueryResult<T, RequestError>), [query, error])
}
