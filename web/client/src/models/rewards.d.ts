export interface Reward {
   id: string
   display: string
   price: number
   description?: string
   duration?: number
   charge?: number
   target?: 'player' | 'team'
   secret?: boolean
}