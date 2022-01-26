import { darken } from 'polished'
import { useMemo, VFC } from 'react'
import { Outlet, useLocation, useMatch, useNavigate } from 'react-router-dom'
import styled, { css } from 'styled-components'

const SelectionBar: VFC<{ values: string[] }> = ({ values, ...props }) => {
   const location = useLocation()
   const match = useMatch(location.pathname)
   const selected = useMemo(() => values.find(v => match?.pathname.startsWith(`/${v}`)), [values, match])
   const navigate = useNavigate()

   return (
      <Style {...props}>
         <Bar>
            {values.map(k => (
               <Button active={selected === k} key={k} onClick={() => navigate(k)}>
                  {k}
               </Button>
            ))}
         </Bar>
         <Outlet />
      </Style>
   )
}

const Button = styled.button<{ active?: boolean }>`
   padding: 0.3em 1em;
   border-radius: 0.3em;
   transition: background 0.1s linear;

   &:hover {
      background: ${p => darken(0.05, p.theme.bg)};
   }

   ${p =>
      p.active &&
      css`
         background: ${p => darken(0.05, p.theme.bg)};
      `}
`

const Bar = styled.div`
   display: grid;
   grid-auto-flow: column;
   align-items: center;
   justify-content: space-evenly;
   gap: 4em;
   margin: 0 auto;

   width: fit-content;

   border: 2px solid ${p => darken(0.05, p.theme.bg)};
   border-radius: 0.5em;
   padding: 0.2em 4em;
   margin-bottom: 1em;
`

const Style = styled.div``

export default SelectionBar
