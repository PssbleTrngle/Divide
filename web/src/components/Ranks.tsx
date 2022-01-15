import { darken } from 'polished'
import { VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'

type Ranks = Record<string, number>

const Ranks: VFC = () => {
   const { data } = useApi<Ranks>('ranks')

   return (
      <Style>
         {data &&
            Object.entries(data).map(([team, rank]) => (
               <p key={rank}>
                  <Rank rank={rank}>#{rank}</Rank> {team}
               </p>
            ))}
      </Style>
   )
}

const COLORS = ['#cea907', '#c7c7c7', '#da892d']

const Rank = styled.div<{ rank: number }>`
   display: inline-block;
   background: ${p => COLORS[p.rank - 1] ?? darken(0.1, p.theme.bg)};
   width: 1em;
   padding: 0.6em;
   color: black;
   border-radius: 99999px;
   margin-right: 1em;
`

const Style = styled.section`
   min-width: 200px;
   grid-area: ranks;
   display: grid;
   gap: 0.5em;
`

export default Ranks
