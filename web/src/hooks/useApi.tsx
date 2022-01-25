import { useState } from 'react'
import { useQuery } from 'react-query'
import useSession from './useSession'

export class RequestError extends Error {
   constructor(public readonly status: number, message: string, public readonly stacktrace?: string[]) {
      super(message)
   }
}

export async function request<T>(url: string, { token, ...config }: Partial<RequestInit> & { token?: string } = {}) {
   const response = await fetch(url, {
      ...config,
      headers: {
         ...config?.headers,
         Authorization: `Bearer ${token}`,
         'Content-Type': 'application/json',
      },
   })

   const json = await response.json().catch(() => null)

   if (!response.ok) throw new RequestError(response.status, json?.message ?? response.statusText, json?.stack)

   return json as T
}

export default function useApi<T>(uri: string) {
   const { token } = useSession()
   const [error, setError] = useState<RequestError>()
   const query = useQuery<T, RequestError, T, string>(uri, () => request<T>(`/api/${uri}`, { token }), {
      onError: setError,
      onSuccess: () => setError(undefined),
   })
   return { ...query, error }
}
