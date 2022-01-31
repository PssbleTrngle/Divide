import styled from 'styled-components'

const Page = styled.section<{ center?: boolean; mini?: boolean }>`
   display: grid;
   align-items: center;
   justify-content: center;
   min-height: ${p => (p.center ? 100 : p.mini ? 0 : 40)}vh;
   text-align: center;

   padding-top: 4rem;
   padding-bottom: 2rem;
`

export default Page
