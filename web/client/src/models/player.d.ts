import { ScoreEvent } from './events.d.ts'
import { Game } from './game.d.ts'

export interface Team {
   name: string
   id: string
   color?: number | string
}

export interface Player {
   name: string
   uuid: string
   team?: Team
   games?: Game<string>[]
   scores?: Array<{
      objective: string
      score: number
      scores?: ScoreEvent[]
   }>
}
