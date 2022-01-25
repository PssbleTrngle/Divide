import { invert, lighten, transparentize } from 'polished'
import styled, { css } from 'styled-components'

export const InputStyles = (color: string) => css`
   background: ${color};

   outline: none;
   border: none;

   transition: all 0.1s ease;

   &:hover {
      background: ${lighten(0.1, color)};
   }

   &:hover,
   &:focus-visible {
      outline: 2px solid ${lighten(0.1, color)};
   }

   &:hover {
      box-shadow: 0 0 0 5px ${transparentize(0.5, color)};
   }

   &:disabled {
      box-shadow: none;
      outline: none;
      background: ${p => lighten(0.2, p.theme.bg)};
      cursor: not-allowed;
   }
`

const Input = styled.input`
   color: ${p => invert(p.theme.text)};
   ${p => InputStyles(p.theme.primary)}

   padding: 0.5em;
   padding-left: 1em;
`

export default Input
