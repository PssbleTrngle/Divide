import { darken, lighten } from 'polished'
import { VFC } from 'react'
import styled, { css } from 'styled-components'
import useApi from '../hooks/useApi'
import Box from './Box'
import { Subtitle } from './Text'

type Ranks = Record<string, number>

const Ranks: VFC = () => {
   const { data } = useApi<Ranks>('ranks')

   return (
      <Style>
         <Subtitle>Ranks</Subtitle>
         {data &&
            Object.entries(data).map(([team, rank]) => (
               <Line key={rank}>
                  <Rank rank={rank}>#{rank}</Rank>
                  <span>{team}</span>
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
