import { useMemo, VFC } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import SelectionBar from './SelectionBar'

const LinkSelectionBar: VFC<{ values: string[] }> = ({ values, ...props }) => {
   const { pathname } = useLocation()
   const last = useMemo(() => pathname.match(/^(?:\/\w+)*\/(\w+)/)?.[1], [pathname])
   const selected = useMemo(() => values.find(v => last === v), [values, last])
   const navigate = useNavigate()

   return <SelectionBar {...props} values={values} value={selected} onChange={v => navigate(v)} />
}

export default LinkSelectionBar
