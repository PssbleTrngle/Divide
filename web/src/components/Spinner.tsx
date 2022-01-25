import { darken, lighten } from 'polished'
import styled, { keyframes } from 'styled-components'

const spin = keyframes`
   from { transform: translateY(-0.5em) scale(0.9, 1) }
   to { transform: translateY(0) scale(1.1, 0.8) }
`

const Spinner = styled.div`
   animation: ${spin} 0.4s ease-in infinite alternate;
   background: ${p => darken(0.1, p.theme.text)};
   height: 1.3em;
   width: 1em;
   clip-path: polygon(50% 0, 101% 20%, 101% 80%, 50% 101%, 0 80%, 0 20%);

   position: relative;

   &::after,
   &::before {
      top: 0;
      left: 0;
      z-index: 1;
      position: absolute;
      content: '';
      height: 100%;
      width: 100%;
   }

   &::before {
      background: ${p => lighten(0.1, p.theme.text)};
      clip-path: polygon(50% 0, 100% 20%, 50% 40%, 0 20%);
   }

   &::after {
      background: ${p => darken(0.3, p.theme.text)};
      clip-path: polygon(50% 40%, 100% 20%, 100% 80%, 50% 100%);
   }
`

export default Spinner
