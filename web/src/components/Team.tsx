import { VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import { Player } from '../hooks/useSession'
import Box from './Box'
import ErrorField from './ErrorField'
import PlayerHead from './PlayerHead'
import { Subtitle } from './Text'

const Team: VFC = () => {
   const { data, error } = useApi<Player[]>('team')

   return (
      <Style>
         <Subtitle>Your Team</Subtitle>
         <ErrorField error={error} />
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

const Style = styled(Box)`
   grid-area: team;
`

export default Team
