import db from "../database.ts"
import { Game } from "models/game.d.ts"

export default db.collection<Game>("games")
