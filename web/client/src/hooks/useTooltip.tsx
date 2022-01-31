import { useEffect } from 'react'
import ReactTooltip from 'react-tooltip'

export default function useTooltip() {
   useEffect(() => {
      ReactTooltip.rebuild()
   })
}
