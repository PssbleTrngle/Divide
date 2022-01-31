import { transparentize } from 'polished'
import { FC, HTMLAttributes } from 'react'
import styled from 'styled-components'
import { Team } from '../models/player'
import { colorOf } from '../util'

const TeamName: FC<Pick<Team, 'color'> & Omit<HTMLAttributes<HTMLSpanElement>, 'color'>> = ({
   children,
   color,
   ...props
}) => (
   <Style {...props} color={colorOf(color)}>
      <b>{children}</b>
   </Style>
)

const Style = styled.span<{ color?: string }>`
   background: ${p => p.color && transparentize(0.8, p.color)};
   color: ${p => p.color};
   width: fit-content;
   min-width: 100px;
   padding: 0.2em 1em;
   border-radius: 0.5em;
   margin: 0 auto;
`

export default TeamName
