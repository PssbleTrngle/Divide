import { darken } from 'polished'
import styled from 'styled-components'

const Banner = styled.div`
   width: 100%;
   background: ${p => p.theme.warning};
   border-bottom: solid 2px ${p => darken(0.1, p.theme.warning)};
   text-align: center;
   padding: 0.5em 0;
   color: black;
   margin-bottom: 1em;

   position: fixed;
   top: 0;
   left: 0;
`

export default Banner
