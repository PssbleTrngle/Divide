export type Status =
   | {
        loading: false
        type: 'saved' | 'running'
     }
   | {
        loading: true
        type: undefined
     }
