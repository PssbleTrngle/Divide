import { Event } from "./events.d.ts";
import { MissionStatus } from "./missions.d.ts";
import { Player } from "./player.d.ts";

export interface GameStatus {
   peaceUntil?: number
   mission?: MissionStatus
   paused: boolean
   started: boolean
}

export interface Game<D = Date> {
   _id: string
   uploadedAt: D
   events: Event[]
   players: Player[]
   startedAt: D
   endedAt: D
}
