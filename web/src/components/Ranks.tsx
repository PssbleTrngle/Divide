import { darken, lighten } from 'polished'
import { VFC } from 'react'
import styled, { css } from 'styled-components'
import useApi from '../hooks/useApi'
import { Team } from '../hooks/useSession'
import Box from './Box'
import ErrorField from './ErrorField'
import TeamName from './TeamName'
import { Subtitle } from './Text'

type Ranks = Array<{
   rank: number
   team: Team
}>

const Ranks: VFC = () => {
   const { data, error } = useApi<Ranks>('ranks')

   return (
      <Style>
         <Subtitle>Ranks</Subtitle>
         <ErrorField error={error} />
         {data?.map(({ rank, team }) => (
            <Line key={rank}>
               <Rank rank={rank}>#{rank}</Rank>
               <TeamName color={team.color}>{team.name}</TeamName>
            </Line>
         ))}
      </Style>
   )
}

const COLORS = ['#cea907', '#b3b3b3', '#cd7f32']

const Badge = (color: string) => css`
   background: linear-gradient(30deg, ${darken(0.1, color)}, ${color}, ${lighten(0.1, color)});
   border: 2px solid ${lighten(0.1, color)};
`

const Rank = styled.div<{ rank: number }>`
   ${p => Badge(COLORS[p.rank - 1] ?? darken(0.1, p.theme.bg))};
   width: 2.5em;
   height: 2.5em;
   color: black;
   border-radius: 99999px;
   margin-right: 1em;

   display: grid;
   justify-content: center;
   align-items: center;
`

const Line = styled.div`
   justify-self: start;
   display: grid;
   justify-content: start;
   grid-auto-flow: column;
   align-items: center;
`

const Style = styled(Box)`
   min-width: 200px;
   grid-area: ranks;
`

export default Ranks
