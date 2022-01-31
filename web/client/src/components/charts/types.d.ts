export interface DataPoint {
   time: number
   value: number
   x?: number
   y?: number
}

export interface SeriesContext {
   unit?: string
   label?: string
   color?: string
   min: number
   max: number
   start: number
   end: number
}
