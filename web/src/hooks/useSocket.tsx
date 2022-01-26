import { createContext, Dispatch, FC, useCallback, useContext, useEffect, useMemo } from 'react'
import { Event, EventType } from '../components/events/types'

const CTX = createContext<WebSocket | null>(null)

function isEvent<T extends EventType>(data: any, type?: T): data is Event<T> {
   return !!data && 'event' in data && 'type' in data && typeof data.event === 'object' && (!type || data.type === type)
}

function useSocket() {
   const socket = useContext(CTX)
   if (!socket) throw new Error('SocketProvider missing')
   return socket
}

export function useSubscribe(listener: Dispatch<MessageEvent>) {
   const socket = useSocket()
   useEffect(() => {
      socket.addEventListener('message', listener)
      return () => socket.removeEventListener('message', listener)
   }, [socket, listener])
}

export function useEvent<T extends EventType>(type: T, consumer: Dispatch<Event<T>>) {
   const listener = useCallback(
      (event: MessageEvent) => {
         const json = JSON.parse(event.data)
         if (isEvent(json, type)) {
            consumer(json)
         }
      },
      [consumer]
   )

   useSubscribe(listener)
}

export function useEvents(consumer: <T extends EventType>(e: Event<T>) => void, ...types: EventType[]) {
   const listener = useCallback(
      (event: MessageEvent) => {
         const json = JSON.parse(event.data)
         if (isEvent(json) && (types.length === 0 || types.includes(json.type))) {
            consumer(json)
         }
      },
      [consumer]
   )

   useSubscribe(listener)
}

export const SocketProvider: FC = ({ children }) => {
   const socket = useMemo(() => {
      const socket = new WebSocket('ws://localhost:8080/api')

      socket.onopen = () => {
         socket.send('ping')
      }

      return socket
   }, [])
   return <CTX.Provider value={socket}>{children}</CTX.Provider>
}
