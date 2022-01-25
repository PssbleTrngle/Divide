import { invert } from 'polished'
import styled from 'styled-components'
import { InputStyles } from './Input'

interface ButtonProps {
   red?: boolean
   size?: number
}

const Button = styled.button<ButtonProps>`
   color: ${p => invert(p.theme.text)};

   ${p => InputStyles(p.theme.primary)};
   ${p => p.red && InputStyles(p.theme.error)};

   padding: ${p => p.size ?? 1}em;

   display: grid;
   align-items: center;
   justify-content: center;
   border-radius: 99999px;

   cursor: pointer;
`

export default Button
