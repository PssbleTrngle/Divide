import { ReactNode } from 'react'

export type Data = ({ type: 'blob' } & DataBlob) | ({ type: 'point' } & DataPoint)

export interface DataProps {
   label?: string
   series: SeriesContext
}

export interface DataPoint {
   id: string
   info?: ReactNode
   time: number
   value: number
   x?: number
   y?: number
}

export interface DataBlob {
   id: string
   info?: ReactNode
   x?: number
   values: number[]
   from: DataPoint
   to: DataPoint
}

export interface SeriesContext {
   label: ReactNode
   color?: string | number
   unit?: string
}

export interface Series extends SeriesContext {
   id: string
   data: DataPoint[]
   hidden?: boolean
}
