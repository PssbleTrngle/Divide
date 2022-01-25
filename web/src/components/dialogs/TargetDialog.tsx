import { FC, useCallback } from 'react'
import { Submit } from '../../hooks/useSubmit'
import { Subtitle } from '../Text'
import Dialog from './Dialog'

const TargetDialog: FC<Submit & { selected?: string }> = ({ send, selected, children, ...props }) => {
   const onSubmit = useCallback(() => {
      if (selected) return send({ target: selected }, true)
   }, [send, selected])

   return (
      <Dialog disabled={!selected} onSubmit={onSubmit} {...props}>
         <Subtitle>Choose Target</Subtitle>
         {children || <i>No valid target</i>}
      </Dialog>
   )
}

export default TargetDialog
