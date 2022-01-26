import { DateTime, Duration, DurationUnit } from 'luxon'
import { useEffect, useState, VFC } from 'react'
import useTooltip from '../../hooks/useTooltip'

const UNITS: (DurationUnit & keyof Duration)[] = ['hours', 'minutes', 'seconds']

const Timestamp: VFC<{ time: number; refresh?: number; update?: boolean }> = ({ refresh = 10, update = true, ...props }) => {
   const [now, updateNow] = useState(DateTime.now())
   const time = DateTime.fromMillis(props.time)
   const diff = time.diff(now, [...UNITS, 'milliseconds'])
   const since = UNITS.filter(u => diff[u] !== 0)
      .map(u => `${-diff[u]}${u.charAt(0)}`)
      .join(' ')

   useEffect(() => {
      if (!update) return
      const interval = setInterval(() => updateNow(DateTime.now()), refresh * 1000)
      return () => clearInterval(interval)
   }, [refresh, update])

   useTooltip()

   return <span data-tip={time.toLocaleString(DateTime.DATETIME_SHORT)}>{since.length ? since : 'moments'} ago</span>
}

export default Timestamp
