import { DispatchWithoutAction, FC, useCallback, useEffect, useState } from 'react'
import styled from 'styled-components'
import { RequestError } from '../../hooks/useApi'
import useDialog from '../../hooks/useDialog'
import Box from '../Box'
import Button from '../Button'
import ErrorField from '../ErrorField'

export interface DialogOptions {
   onCancel?: DispatchWithoutAction
   onSubmit?: DispatchWithoutAction
   disabled?: boolean
}

const Dialog: FC<DialogOptions> = ({ children, disabled, onCancel, onSubmit, ...props }) => {
   const { close } = useDialog()
   const [error, setError] = useState<RequestError>()

   useEffect(() => {
      setError(undefined)
   }, [])

   const wrap = useCallback(
      (callback?: DispatchWithoutAction) => async () => {
         try {
            await callback?.()
            close()
         } catch (e) {
            setError(e as RequestError)
         }
      },
      [close]
   )

   return (
      <Style {...props}>
         <DialogErrorField error={error} />
         <Content>{children}</Content>
         <SubmitButton disabled={disabled} onClick={wrap(onSubmit)}>
            OK
         </SubmitButton>
         <CancelButton red onClick={wrap(onCancel)}>
            Cancel
         </CancelButton>
      </Style>
   )
}

const SubmitButton = styled(Button)`
   margin-top: 2em;
   grid-area: submit;
   font-size: 0.8rem;
`

const CancelButton = styled(Button)`
   margin-top: 2em;
   grid-area: cancel;
   font-size: 0.8rem;
`

const DialogErrorField = styled(ErrorField)`
   grid-area: error;
`

const Content = styled.section`
   grid-area: content;
`

const Style = styled(Box)`
   position: fixed;
   top: 50%;
   left: 50%;
   transform: translate(-50%, -50%);
   text-align: center;

   z-index: 100;

   display: grid;
   grid-template:
      'error error'
      'content content'
      'submit cancel'
      / 1fr 1fr;
`

export default Dialog
