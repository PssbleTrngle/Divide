import { Settings } from 'luxon'
import React from 'react'
import ReactDOM from 'react-dom'
import { QueryClient, QueryClientProvider, setLogger } from 'react-query'
import { BrowserRouter } from 'react-router-dom'
import { createGlobalStyle, ThemeProvider } from 'styled-components'
import App from './App'
import { DialogProvider } from './hooks/useDialog'
import { SessionProvider } from './hooks/useSession'
import { SocketProvider } from './hooks/useSocket'
import { StatusProvider } from './hooks/useStatus'
import reportWebVitals from './reportWebVitals'
import './styles/fonts.css'
import './styles/reset.css'
import dark from './themes/dark'

Settings.defaultLocale = 'de'

setLogger({
   log: () => {},
   error: () => {},
   warn: () => {},
})

const Global = createGlobalStyle`
  body, html {
    background: ${p => p.theme.bg};
    color: ${p => p.theme.text};
    font-family: 'Open Sans', sans-serif;
  }

  li, ul, ol {
     list-style: none;
  }
`

const client = new QueryClient({
   defaultOptions: {
      queries: {
         refetchInterval: 2000,
         retry: false,
      },
   },
})

ReactDOM.render(
   <React.StrictMode>
      <BrowserRouter>
         <ThemeProvider theme={dark}>
            <Global />
            <QueryClientProvider client={client}>
               <StatusProvider>
                  <SessionProvider>
                     <SocketProvider>
                        <DialogProvider>
                           <App />
                        </DialogProvider>
                     </SocketProvider>
                  </SessionProvider>
               </StatusProvider>
            </QueryClientProvider>
         </ThemeProvider>
      </BrowserRouter>
   </React.StrictMode>,
   document.getElementById('root')
)

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals()
