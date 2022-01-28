import { invert, mix } from 'polished'
import { memo, useCallback, useMemo, useState, VFC } from 'react'
import { animated, useTransition } from 'react-spring'
import styled from 'styled-components'
import { useEvents } from '../hooks/useSocket'
import EventLine from './events/EventLine'
import { Event } from './events/types'

const config = { tension: 125, friction: 20, precision: 0.1 }
const timeout = 3000

const Messages: VFC = () => {
   const [messages, setMessage] = useState<Event[]>([])
   const refMap = useMemo(() => new WeakMap(), [])
   const addMessage = useCallback((m: Event) => setMessage(a => [...a, m]), [setMessage])

   useEvents(addMessage)

   const transitions = useTransition(messages, {
      from: { opacity: 0, height: 0, life: '100%' },
      config: (_item, _i, phase) => key => phase === 'enter' && key === 'life' ? { duration: timeout } : config,
      keys: m => m.id,
      enter: m => async next => {
         await next({ opacity: 1, height: refMap.get(m).offsetHeight })
         await next({ life: '0%' })
      },
      leave: [{ opacity: 0 }, { height: 0 }],
      onRest: (_r, _c, item) => {
         setMessage(a => a.filter(m => m.id !== item.id))
      },
   })

   return (
      <Style>
         {transitions(({ life, ...style }, msg) => (
            <Bubble key={msg.id} style={style}>
               <div ref={r => r && refMap.set(msg, r)}>
                  <CustomEventLine {...msg} />
                  <Life style={{ width: life }} />
               </div>
            </Bubble>
         ))}
      </Style>
   )
}

const Life = styled(animated.div)`
   position: absolute;
   bottom: 0;
   left: 0;
   background: ${p => p.theme.accent};
   height: 0.2em;
`

const CustomEventLine = styled(EventLine)`
   padding: 0.8em 2em;
   background: ${p => mix(0.7, p.theme.text, p.theme.bg)};
   color: ${p => invert(p.theme.text)};
`

const Bubble = styled(animated.div)`
   border-radius: 0.5em;
   width: fit-content;
   margin: 0 auto;
   margin-top: 0.5em;
   box-sizing: border-box;
   position: relative;

   box-shadow: 0.2em 0.3em 0.5em 0 #0005;

   overflow: hidden;
`

const Style = styled.section`
   position: fixed;
   z-index: 50;
   width: 100%;
   display: grid;
   justify-content: end;
   padding-right: 1em;
`

export default memo(Messages)
