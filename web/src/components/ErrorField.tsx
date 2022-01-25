import { VFC } from 'react'
import { RequestError } from '../hooks/useApi'

const ErrorField: VFC<{ error?: RequestError | null }> = ({ error }) => {
   if (!error) return null
   return <p>{error.message}</p>
}

export default ErrorField
