import { DateTime } from 'luxon'
import { VFC } from 'react'
import { Game } from '../models/game'
import Link from './Link'

const GameLink: VFC<Game<string>> = ({ _id, startedAt, name }) => (
   <Link to={`/game/${_id}`}>{name} - {DateTime.fromISO(startedAt).toLocaleString()}</Link>
)

export default GameLink
