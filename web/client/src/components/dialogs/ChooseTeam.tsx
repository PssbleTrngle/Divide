import { useState, VFC } from 'react'
import styled, { css } from 'styled-components'
import useApi from '../../hooks/useApi'
import { Submit } from '../../hooks/useSubmit'
import { Team } from '../../models/player'
import TeamName from '../TeamName'
import TargetDialog from './TargetDialog'

const ChooseTeam: VFC<Submit> = props => {
   const { data } = useApi<Team[]>('team?opponent=true')
   const [selected, setSelected] = useState<string>()

   return (
      <TargetDialog selected={selected} {...props}>
         {!!data?.length && (
            <section>
               {data?.map(({ name, id, ...team }) => (
                  <TeamRow key={id} selected={selected === id} {...team} onClick={() => setSelected(id)}>
                     {name}
                  </TeamRow>
               ))}
            </section>
         )}
      </TargetDialog>
   )
}

const TeamRow = styled(TeamName)<{ selected?: boolean }>`
   ${p =>
      p.selected &&
      css`
         background: ${p => p.theme.primary};
      `}
`

export default ChooseTeam
