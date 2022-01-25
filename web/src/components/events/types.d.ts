import { Player, Team } from '../../hooks/useSession'

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

export interface DeathEvent {
   player: Player
   killer?: Player
   pos: Position
   source: string
}

export interface RewardEvent {
   reward: string
   boughtBy: Player
   target?: Player | Team
   pointsPaid: number
   pointsNow: number
}

export interface ActionEvent {
   action: string
   reward: string
   boughtBy?: Player
   target?: Player | Team
}

export interface EventTypes {
   cycle_event: CycleEvent
   loot_crate_filled: LootFillEvent
   death: DeathEvent
   reward: RewardEvent
   action: ActionEvent
   game: GameEvent
}

export interface GameEvent {
   action: string
}

export type EventType = keyof EventTypes
