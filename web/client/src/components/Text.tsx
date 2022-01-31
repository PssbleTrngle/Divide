import { darken } from 'polished'
import styled from 'styled-components'

export const Colored = styled.b<{ color?: string }>`
   color: ${p => p.color ?? p.theme.accent};
`

export const Subtitle = styled.h3`
   margin-bottom: 1em;
`

export const Title = styled.h1`
   font-size: 2rem;
   margin-bottom: 1em;
`

export const Code = styled.code`
   background: ${p => darken(0.05, p.theme.bg)};
   padding: 0.3em;
   border-radius: 0.3em;
   margin: 0 0.2em;
`
