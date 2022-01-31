import { transparentize } from 'polished'
import { VFC } from 'react'
import styled from 'styled-components'
import { RequestError } from '../hooks/useApi'

const ErrorField: VFC<{ error?: RequestError | null }> = ({ error, ...props }) => {
   if (!error?.status) return null
   if (error.status === 500) return null
   return (
      <Style {...props}>
         <i>{error.message}</i>
      </Style>
   )
}

const Style = styled.p`
   background: ${p => transparentize(0.5, p.theme.error)};
   border: 1px solid ${p => p.theme.error};
   padding: 0.2em;
   border-radius: 0.5em;
`

export default ErrorField
