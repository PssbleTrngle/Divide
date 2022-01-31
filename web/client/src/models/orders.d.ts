export interface Order {
   id: string
   item: string
   cost: number
   max?: number
}

export interface BoughtOrder {
   order: Order
   amount: number
}
