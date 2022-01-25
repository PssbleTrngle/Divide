import { darken, mix } from 'polished'
import { css } from 'styled-components'

export const biColorGradient = (first: string, second: string) => css`
   background: linear-gradient(
      120deg,
      ${mix(0.4, second, first)},
      ${first} 40%,
      ${first} 80%,
      ${darken(0.1, first)}
   );
`
