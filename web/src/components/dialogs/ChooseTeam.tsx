import { useState, VFC } from 'react'
import styled, { css } from 'styled-components'
import useApi from '../../hooks/useApi'
import { Team } from '../../hooks/useSession'
import { Submit } from '../../hooks/useSubmit'
import TargetDialog from './TargetDialog'

const ChooseTeam: VFC<Submit> = props => {
   const { data } = useApi<Team[]>('team?opponent=true')
   const [selected, setSelected] = useState<string>()

   return (
      <TargetDialog selected={selected} {...props}>
         {!!data?.length && (
            <section>
               {data?.map(({ name }) => (
                  <TeamRow key={name} selected={selected === name} onClick={() => setSelected(name)}>
                     {name}
                  </TeamRow>
               ))}
            </section>
         )}
      </TargetDialog>
   )
}

const TeamRow = styled.p<{ selected?: boolean }>`
   ${p =>
      p.selected &&
      css`
         background: ${p => p.theme.primary};
      `}
`

export default ChooseTeam
