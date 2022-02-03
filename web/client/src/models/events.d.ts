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
   dimension: string
}

export interface LootEvent {
   pos: Position
}

export interface LootFillEvent extends LootEvent {
   table: string
}

export interface LootNotifyEvent extends LootEvent {
   team: Team
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
   target?: Target
   pointsPaid: number
   pointsNow: number
}

export type Target = Player | Team

export interface ActionEvent {
   action: string
   reward: string
   boughtBy?: Player
   target?: Target
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
   loot_crate_notify: LootNotifyEvent
   loot_crate_cleaned: LootEvent
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
