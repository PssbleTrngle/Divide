export interface MissionStatus {
   mission: Mission
   secondsLeft: number
   done: boolean
}

export interface Mission {
   description: string
}