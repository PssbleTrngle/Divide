import { useEffect, VFC } from 'react'

const Head: VFC<{ title?: string }> = ({ title }) => {
   useEffect(() => {
      document.title = title ? `Divide - ${title}` : 'Divide'
   }, [title])
   return <></>
}

export default Head
