import { VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'

interface Status {
   test: string
}

const Status: VFC = () => {
   const { data } = useApi<Status>('status')

   return <Style>{JSON.stringify(data)}</Style>
}

const Style = styled.section`
   display: grid;
   gap: 1rem;
`

export default Status
