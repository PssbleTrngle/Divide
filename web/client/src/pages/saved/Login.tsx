import { useEffect, useState, VFC } from 'react'
import Box from '../../components/Box'
import Button from '../../components/Button'
import ErrorField from '../../components/ErrorField'
import Input from '../../components/Input'
import Page from '../../components/Page'
import { Title } from '../../components/Text'
import useSession from '../../hooks/useSession'
import useSubmit from '../../hooks/useSubmit'

const Login: VFC = () => {
   const [password, setPassword] = useState('')
   const session = useSession()
   const login = useSubmit<{ token: string }>('auth', { data: { password } })

   useEffect(() => {
      if (login.data) session.login(login.data.token)
   }, [login.data, session])

   return (
      <Page>
         <Title>Enter Admin Password</Title>
         <Box as='form' onSubmit={login.send}>
            <ErrorField error={login.error} />
            <Input placeholder='password' value={password} onChange={e => setPassword(e.target.value)} />
            <Button>Login</Button>
         </Box>
      </Page>
   )
}

export default Login
