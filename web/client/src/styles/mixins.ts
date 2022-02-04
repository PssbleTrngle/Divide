import { darken, mix } from 'polished'
import { css, keyframes } from 'styled-components'

export const biColorGradient = (first: string, second: string) => css`
   background: linear-gradient(120deg, ${mix(0.4, second, first)}, ${first} 40%, ${first} 80%, ${darken(0.1, first)});
`

export const pseudo = css`
   position: absolute;
   content: '';
   top: 0;
   left: 0;
   height: 100%;
   width: 100%;
`

export const shimmer = (size?: string) => keyframes`
   from { background-position: 0 0 }
   to { background-position: ${size ?? '1em'} 0 }
`

export const loading = css<{ size?: string; animate?: boolean }>`
   position: relative;
   overflow: hidden;

   &::before {
      ${pseudo};
      will-change: background-position;
      transform: rotate(30deg) scale(2);
      ${p =>
         p.animate &&
         css`
            animation: ${shimmer(p.size)} 1s linear infinite;
         `};
      background-image: linear-gradient(-90deg, #0001, #0005, #0001);
      background-repeat: repeat-x;
   }
`
