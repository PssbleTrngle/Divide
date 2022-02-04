import { FC } from 'react'
import styled from 'styled-components'
import { ButtonLink } from './Link'

const BackLink: FC = ({ children }) => <Style to='./..'>{children ?? 'Back'}</Style>

const Style = styled(ButtonLink)`
   margin-bottom: 2em;
`

export default BackLink
