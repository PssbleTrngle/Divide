import { DateTime, Duration, DurationUnit } from 'luxon'
import { memo, useEffect, useMemo, useState, VFC } from 'react'
import useTooltip from '../../hooks/useTooltip'

const UNITS: (DurationUnit & keyof Duration)[] = ['hours', 'minutes', 'seconds']

const Timestamp: VFC<{ time: number; refresh?: number; update?: boolean }> = ({
   refresh = 10,
   update = true,
   ...props
}) => {
   const [now, updateNow] = useState(DateTime.now())
   const time = useMemo(() => DateTime.fromMillis(props.time), [props.time])

   const tooltip = useMemo(() => time.toLocaleString(DateTime.DATETIME_SHORT), [time])

   const text = useMemo(() => {
      const diff = time.diff(now, [...UNITS, 'milliseconds'])
      console.log(diff.toMillis())
      if (diff.toMillis() <= -1000 * 60 * 10) return time.toLocaleString(DateTime.TIME_SIMPLE)
      const since = UNITS.filter(u => diff[u] !== 0)
         .map(u => `${-diff[u]}${u.charAt(0)}`)
         .join(' ')

      return `${since.length ? since : 'moments'} ago`
   }, [time, tooltip])

   useEffect(() => {
      if (!update) return
      const interval = setInterval(() => updateNow(DateTime.now()), refresh * 1000)
      return () => clearInterval(interval)
   }, [refresh, update])

   useTooltip()

   return <span data-tip={tooltip}>{text}</span>
}

export default memo(Timestamp)
