import { darken } from 'polished'
import styled from 'styled-components'

const Box = styled.section`
   background: ${p => darken(0.05, p.theme.bg)};
   padding: 1em;
   border-radius: 0.5em;
   
   display: grid;
   gap: 0.5em;
   align-items: center;
   justify-content: center;
`

export default Box
