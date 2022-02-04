import { Link as BaseLink } from 'react-router-dom'
import styled from 'styled-components'
import { ButtonStyles } from './Button'

const Link = styled(BaseLink)`
   color: ${p => p.theme.text};
   text-decoration: none;

   &:hover {
      text-decoration: underline;
   }
`

export const ButtonLink = styled(Link)`
   ${ButtonStyles};
   margin: 0 auto;
`

export default Link
