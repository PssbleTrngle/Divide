import { Mission } from './missions.d.ts'
import { Order } from './orders.d.ts'
import { Player, Team } from './player.d.ts'

export interface Event<Type extends EventType = EventType> {
   gameTime: number
   realTime: number
   type: Type
   event: EventTypes[Type]
   id: string
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

export interface EraEvent {
   era: string
}

export interface BorderEvent {
   action: string
}

export interface MissionEvent {
   mission: Mission
   action: string
   team?: Team
}

export interface BountyEvent {
   pointsEarned: number
   pointsNow: number
   doneAlready: number
   fulfilledBy: Player
   bounty: {
      description: string
      amount: number
   }
}

export interface GameEvent {
   action: string
}

export interface OrderEvent {
   order: Order
   amount: number
   cost: number
   pointsNow: number
   orderedBy: Player
}

export interface ScoreEvent {
   player: Player
   objective: string
   score: number
}

export interface PointsEvent {
   before: number
   now: number
   type: 'total' | 'current'
   team: Team
}

export interface EventTypes {
   cycle_event: CycleEvent
   loot_crate_filled: LootFillEvent
   death: DeathEvent
   reward: RewardEvent
   action: ActionEvent
   game: GameEvent
   eras: EraEvent
   border: BorderEvent
   mission: MissionEvent
   bounty: BountyEvent
   order: OrderEvent
   score: ScoreEvent
   points: PointsEvent
}

export type EventType = keyof EventTypes
