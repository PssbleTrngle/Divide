import { useMemo, VFC } from 'react'
import { useLocation, useMatch, useNavigate } from 'react-router-dom'
import SelectionBar from './SelectionBar'

const LinkSelectionBar: VFC<{ values: string[] }> = ({ values, ...props }) => {
   const location = useLocation()
   const match = useMatch(location.pathname)
   const selected = useMemo(() => values.find(v => match?.pathname.startsWith(`/${v}`)), [values, match])
   const navigate = useNavigate()

   return <SelectionBar {...props} values={values} value={selected} onChange={v => navigate(v)} />
}

export default LinkSelectionBar
