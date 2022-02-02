import { transparentize } from 'polished'
import { MouseEvent, useCallback, VFC } from 'react'
import styled from 'styled-components'
import useChartContext from '../../hooks/useChart'
import Box from '../Box'
import { Series } from './types'

const Legend: VFC = () => {
   const { series, setHidden } = useChartContext()

   const onClick = useCallback(
      ({ id }: Series) =>
         (e: MouseEvent) => {
            setHidden(a => {
               const diff = a.filter(it => it !== id)
               if (e.shiftKey) {
                  if (diff.length >= series.length - 1) return []
                  return series.map(s => s.id).filter(it => it !== id)
               }
               if (a.includes(id)) return diff
               return [...a, id]
            })
         },
      [setHidden, series]
   )

   return (
      <Style>
         {series.map(series => (
            <Entry key={series.id}>
               {series.label}
               <Toggle active={!series.hidden} onClick={onClick(series)}>
                  👁
               </Toggle>
            </Entry>
         ))}
      </Style>
   )
}

const Toggle = styled.span.attrs({ role: 'button' })<{ active: boolean }>`
   cursor: pointer;
   user-select: none;
   color: ${p => transparentize(p.active ? 0 : 0.6, p.theme.text)};
   justify-self: end;
`

const Entry = styled.div`
   display: grid;
   grid-auto-flow: column;
   align-items: center;
   gap: 1em;
`

const Style = styled(Box)`
   grid-area: legend;
   align-items: center;
   height: fit-content;
   gap: 1em;
`

export default Legend
