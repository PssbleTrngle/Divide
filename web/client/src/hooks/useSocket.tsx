import { createContext, Dispatch, FC, useCallback, useContext, useEffect, useMemo } from 'react'
import { Event, EventType } from '../models/events'

const CTX = createContext<WebSocket | null>(null)

function isEvent<T extends EventType>(data: unknown, type?: T): data is Event<T> {
   return (
      !!data &&
      typeof data === 'object' &&
      'event' in data &&
      'type' in data &&
      typeof (data as Event).event === 'object' &&
      (!type || (data as Event).type === type)
   )
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
      [consumer, type]
   )

   useSubscribe(listener)
}

export const EXCLUDED_EVENTS: EventType[] = ['score', 'points']

export function useEvents(
   consumer: <T extends EventType>(e: Event<T>) => void,
   types: EventType[] | ((t: EventType) => boolean) = t => !EXCLUDED_EVENTS.includes(t)
) {
   const predicate = useMemo(() => (Array.isArray(types) ? (t: EventType) => types.includes(t) : types), [types])

   const listener = useCallback(
      (event: MessageEvent) => {
         const json = JSON.parse(event.data)
         if (isEvent(json) && (types.length === 0 || predicate(json.type))) {
            consumer(json)
         }
      },
      [consumer, predicate, types.length]
   )

   useSubscribe(listener)
}

const socketURL = `ws://${window.location.hostname}:8080/api`

export const SocketProvider: FC = ({ children }) => {
   const socket = useMemo(() => {
      const socket = new WebSocket(socketURL)

      socket.onopen = () => {
         socket.send('ping')
      }

      return socket
   }, [])
   return <CTX.Provider value={socket}>{children}</CTX.Provider>
}
