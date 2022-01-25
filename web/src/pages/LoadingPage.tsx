import { VFC } from 'react'
import styled from 'styled-components'
import Page from '../components/Page'
import Spinner from '../components/Spinner'

const LoadingPage: VFC = () => (
   <Page center>
      <section>
         <BigSpinner />
         <p>Loading</p>
      </section>
   </Page>
)

const BigSpinner = styled(Spinner)`
   font-size: 5rem;
`

export default LoadingPage
