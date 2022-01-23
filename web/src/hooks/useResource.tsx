import useApi from './useApi'

export interface Resource<T> {
   id: string
   value: T
}

export default function useResource<T>(uri: string) {
   return useApi<Resource<T>[]>(uri)
}
