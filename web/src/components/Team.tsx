import { VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import { Player } from '../hooks/useSession'
import PlayerRow from './PlayerRow'

const Team: VFC = () => {
   const { data } = useApi<Player[]>('team')

   return (
      <Style>
         {data?.map(p => (
            <PlayerRow key={p.uuid} {...p} />
         ))}
      </Style>
   )
}

const Style = styled.section`
   display: grid;
   gap: 1rem;
`

export default Team
