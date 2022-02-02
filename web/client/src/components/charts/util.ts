import { DateTime } from 'luxon'
import useChartContext from '../../hooks/useChart'
import { Data, DataPoint } from './types'

function pad(percentage: number, padding = 0.05) {
   return percentage * (1 - padding * 2) + padding * 100
}

export function formatTime(time: number) {
   return DateTime.fromMillis(time).toLocaleString(DateTime.TIME_WITH_SECONDS)
}

export function usePointPos({ time, value, ...pos }: Omit<DataPoint, 'id'>) {
   const { min, max, start, end } = useChartContext()
   const x = pad(pos.x ? pos.x * 100 : relativePos(start, end, time))
   const y = pad(pos.y ? pos.y * 100 : relativePos(min, max, value, true))
   return { x, y }
}

function relativePos(min: number, max: number, value: number, invert = false) {
   const percentage = ((value - min) / (max - min)) * 100
   return invert ? 100 - percentage : percentage
}


export function pointsOf(data: Data) {
   switch (data.type) {
      case 'blob':
         return { from: data.from, to: { ...data.to, x: data.from.x, time: data.from.time } }
      case 'point':
         return { from: data, to: data }
   }
}