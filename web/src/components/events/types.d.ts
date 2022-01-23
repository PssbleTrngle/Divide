export interface Event<Type extends EventType = EventType> {
   gameTime: number
   realTime: number
   type: Type
   event: EventTypes[Type]
}

export interface CycleEvent {
   id: string
   action: string
   timesRun?: number
   pause?: number
}

export interface Position {
   x: number
   y: number
   z: number
}

export interface LootFillEvent {
   pos: Position
   dimension: string
   table: string
}

export interface EventTypes {
   cycle_event: CycleEvent
   loot_crate_filled: LootFillEvent
}

export type EventType = keyof EventTypes