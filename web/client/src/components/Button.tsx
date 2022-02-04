import { invert } from 'polished'
import styled, { css } from 'styled-components'
import { InputStyles } from './Input'

interface ButtonProps {
   red?: boolean
   size?: number
}

export const ButtonStyles = css<ButtonProps>`
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

const Button = styled.button<ButtonProps>`
   ${ButtonStyles}
`

export default Button
