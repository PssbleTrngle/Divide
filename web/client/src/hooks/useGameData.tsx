import { useMemo } from 'react'
import { useParams } from 'react-router-dom'
import useApi, { ApiConfig } from './useApi'

export default function useGameData<T>(endpoint: string, config?: ApiConfig<T>) {
   const { game } = useParams()
   const url = useMemo(() => (game ? `game/${game}/${endpoint}` : endpoint), [game, endpoint])
   return useApi<T>(url, config)
}
