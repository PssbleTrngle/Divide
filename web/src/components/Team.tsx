import { VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import { Player } from '../hooks/useSession'
import PlayerHead from './PlayerHead'

const Team: VFC = () => {
   const { data } = useApi<Player[]>('team')

   return (
      <Style>
         <h3>Your Team</h3>
         <Heads>
            {data?.map(p => (
               <PlayerHead key={p.uuid} {...p} />
            ))}
         </Heads>
      </Style>
   )
}

const Heads = styled.section`
   display: grid;
   grid-template-columns: repeat(2, 1fr);
   gap: 5px;
`

const Style = styled.section`
   grid-area: team;
`

export default Team
