import { darken } from 'polished'
import { Dispatch, VFC } from 'react'
import { Outlet } from 'react-router-dom'
import styled, { css } from 'styled-components'

const SelectionBar: VFC<{
   values: string[]
   onChange: Dispatch<string>
   value?: string
}> = ({ values, onChange, value, ...props }) => (
   <Style {...props}>
      <Bar narrow={values.length > 4}>
         {values.map(k => (
            <Button active={value === k} key={k} onClick={() => onChange(k)}>
               {k}
            </Button>
         ))}
      </Bar>
      <Outlet />
   </Style>
)

const Button = styled.button<{ active?: boolean }>`
   padding: 0.3em 1em;
   border-radius: 0.3em;
   transition: background 0.1s linear;
   text-transform: capitalize;

   &:hover {
      background: ${p => darken(0.05, p.theme.bg)};
   }

   ${p =>
      p.active &&
      css`
         background: ${p => darken(0.05, p.theme.bg)};
      `}
`

const Bar = styled.nav<{ narrow?: boolean }>`
   display: grid;
   grid-auto-flow: column;
   align-items: center;
   justify-content: space-evenly;
   gap: ${p => (p.narrow ? 1 : 4)}em;
   margin: 0 auto;

   width: fit-content;
   max-width: 800px;

   border: 2px solid ${p => darken(0.05, p.theme.bg)};
   border-radius: 0.5em;
   padding: 0.2em 4em;
   margin-bottom: 1em;
`

const Style = styled.div``

export default SelectionBar
