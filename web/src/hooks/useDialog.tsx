import { createContext, Dispatch, DispatchWithoutAction, FC, ReactNode, useCallback, useContext, useState } from 'react'
import styled from 'styled-components'

const CTX = createContext<{
   open: Dispatch<ReactNode>
   close: DispatchWithoutAction
}>({
   open: () => console.warn('DialogProvider missing'),
   close: () => console.warn('DialogProvider missing'),
})

export default function useDialog() {
   return useContext(CTX)
}

export const DialogProvider: FC = ({ children }) => {
   const [dialog, open] = useState<ReactNode>(null)

   const close = useCallback(() => open(null), [open])

   return (
      <CTX.Provider value={{ open, close }}>
         {dialog && (
            <>
               <Curtain onClick={close} />
               {dialog}
            </>
         )}
         {children}
      </CTX.Provider>
   )
}

const Curtain = styled.section`
   position: fixed;
   top: 0;
   left: 0;
   width: 100vw;
   height: 100vh;
   background: #0002;
   z-index: 99;
`
