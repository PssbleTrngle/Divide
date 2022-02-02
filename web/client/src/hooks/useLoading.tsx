import { FC } from 'react'
import { UseQueryResult } from 'react-query'
import LoadingPage from '../pages/LoadingPage'
import NotFound from '../pages/NotFound'
import { RequestError } from './useApi'

export default function useLoading<T>(
   { data, error }: UseQueryResult<T, RequestError>,
   render: (value: T) => ReturnType<FC>
) {
   if (error?.status === 404) return <NotFound />
   if (!data) return <LoadingPage />

   return render(data)
}
