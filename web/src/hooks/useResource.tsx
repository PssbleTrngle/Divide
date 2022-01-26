import { useMemo } from 'react'
import useApi from './useApi'

interface Resource<T> {
   id: string
   value: T
}

export default function useResource<T>(uri: string) {
   const { data, ...rest } = useApi<Resource<T>[]>(uri)
   const transformed = useMemo(() => data?.map(({ value, id }) => ({ ...value, id })), [data])
   return { ...rest, data: transformed }
}
