import { useEffect, useState, VFC } from 'react'
import useChartContext from '../../hooks/useChart'
import { Data, DataProps } from './types'

const DataWrapper: VFC<
   Data &
      DataProps & {
         children: (p: { hovered: boolean }) => ReturnType<VFC>
      }
> = ({ children, ...props }) => {
   const { hover } = useChartContext()

   const [hovered, setHovered] = useState(false)

   useEffect(() => {
      if (hovered) hover(props)
      else hover()
   }, [hovered, hover, props])

   return (
      <g data-for='chart' data-tip='' onMouseEnter={() => setHovered(true)} onMouseLeave={() => setHovered(false)}>
         {children({ ...props, hovered })}
      </g>
   )
}
export default DataWrapper
