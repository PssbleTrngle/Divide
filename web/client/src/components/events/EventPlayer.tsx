import { useMemo, VFC } from 'react'
import styled from 'styled-components'
import { useInChart } from '../../hooks/useChart'
import useGameData from '../../hooks/useGameData'
import { Player } from '../../models/player'
import { colorOf } from '../../util'
import PlayerHead from '../PlayerHead'
import { Colored } from '../Text'

const EventPlayer: VFC<Player & { color?: string; onlyHead?: boolean }> = ({ color, onlyHead, ...initial }) => {
   const { data } = useGameData(`player/${initial.uuid}`, { initialData: initial })
   const player = useMemo(() => data ?? initial, [data, initial])
   const actualColor = color ?? colorOf(player?.team?.color)

   const isChart = useInChart()

   return (
      <>
         <InlineHead highlight={actualColor} {...player} size='1.4em' />
         {!(onlyHead ?? isChart) && <Colored color={actualColor}>{player.name}</Colored>}
      </>
   )
}

const InlineHead = styled(PlayerHead)`
   &:not(&:first-child) {
      margin-left: 0.2em;
   }
`

export default EventPlayer
