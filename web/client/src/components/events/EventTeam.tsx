import { VFC } from 'react'
import styled from 'styled-components'
import { useInChart } from '../../hooks/useChart'
import useTooltip from '../../hooks/useTooltip'
import { Team } from '../../models/player'
import { colorOf } from '../../util'
import { Colored } from '../Text'

const EventTeam: VFC<Team & { onlyHead?: boolean }> = ({ onlyHead, ...team }) => {
   useTooltip()
   const isChart = useInChart()
   const color = colorOf(team.color)

   return (
      <>
         <InlineHead data-tip={team.name} color={color} />
         {!(onlyHead ?? isChart) && <Colored color={color}>{team.name}</Colored>}
      </>
   )
}

const InlineHead = styled.div<{ color?: string }>`
   background: ${p => p.color};

   &:not(&:first-child) {
      margin-left: 0.2em;
   }

   width: 1.4em;
   height: 1.4em;
`

export default EventTeam
