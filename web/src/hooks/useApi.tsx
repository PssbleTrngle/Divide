import { useQuery } from 'react-query'
import useSession from './useSession'

export class RequestError extends Error {
   constructor(public readonly status: number, message: string) {
      super(message)
   }
}

export async function get<T>(url: string, token?: string) {
   const response = await fetch(url, {
      headers: {
         Authorization: `Bearer ${token}`,
      },
   })

   if (!response.ok) throw new RequestError(response.status, response.statusText)

   const json = await response.json()
   return json as T
}

export default function useApi<T>(uri: string) {
   const { token } = useSession()
   return useQuery(uri, () => get<T>(`api/${uri}`, token))
}
