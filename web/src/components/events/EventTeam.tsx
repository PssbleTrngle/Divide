import { VFC } from 'react'
import styled from 'styled-components'
import { Team } from '../../hooks/useSession'
import useTooltip from '../../hooks/useTooltip'
import { colorOf } from '../../util'
import { Colored } from '../Text'

const EventTeam: VFC<Team> = team => {
   useTooltip()

   const color = colorOf(team.color)

   return (
      <>
         <InlineHead data-tip={team.name} color={color} />
         <Colored color={color}> {team.name}</Colored>
      </>
   )
}

const InlineHead = styled.div<{ color?: string }>`
   margin-right: -0.1em;

   background: ${p => p.color};

   width: 1.4em;
   height: 1.4em;
`

export default EventTeam
