import styled from 'styled-components'

const Page = styled.section<{ center?: boolean }>`
   display: grid;
   align-items: center;
   justify-content: center;
   min-height: ${p => (p.center ? 100 : 40)}vh;
   text-align: center;

   padding-top: 1rem;
`

export default Page
