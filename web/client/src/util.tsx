import { Duration, DurationUnit } from 'luxon'

export function delay<T>(data: T, error = true) {
   return () =>
      new Promise<T>((res, rej) => {
         setTimeout(() => {
            if (error && Math.random() < 0.1) rej(new Error('an error occured'))
            else res(data)
         }, 100 + 300 * Math.random())
      })
}

export function colorOf(value?: number) {
   if (!value) return undefined
   const red = (value >> 16) & 0xff
   const green = (value >> 8) & 0xff
   const blue = (value >> 0) & 0xff
   return `rgb(${red}, ${green},  ${blue})`
}

export function exists<T>(value: T | null | undefined): value is T {
   return (value ?? null) !== null
}

const UNITS: DurationUnit[] = ['hours', 'minutes', 'seconds']

export function formatDuration(seconds: number) {
   const duration = Duration.fromMillis(seconds * 1000).shiftTo(...UNITS)
   return UNITS.map(unit => ({ unit, value: Math.floor(duration.get(unit)) }))
      .filter(it => it.value > 0)
      .map(({ unit, value }) => `${value}${unit.charAt(0)}`)
      .join(' ')
}
