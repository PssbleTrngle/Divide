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
