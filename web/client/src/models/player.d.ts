export interface Team {
   name: string
   id: string
   color?: number
}

export interface Player {
   name: string
   uuid: string
   team?: Team
}
