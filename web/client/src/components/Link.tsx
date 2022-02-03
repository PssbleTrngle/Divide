import { Link as BaseLink } from 'react-router-dom'
import styled from 'styled-components'

const Link = styled(BaseLink)`
   color: ${p => p.theme.text};
   text-decoration: none;

   &:hover {
      text-decoration: underline;
   }
`

export default Link
