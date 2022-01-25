import { Settings } from 'luxon'
import React, { FC } from 'react'
import ReactDOM from 'react-dom'
import { QueryClient, QueryClientProvider, setLogger, useQuery } from 'react-query'
import { BrowserRouter } from 'react-router-dom'
import { createGlobalStyle, ThemeProvider } from 'styled-components'
import App from './App'
import Banner from './components/Banner'
import { request } from './hooks/useApi'
import { DialogProvider } from './hooks/useDialog'
import { SessionProvider } from './hooks/useSession'
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
`

const client = new QueryClient({
   defaultOptions: {
      queries: {
         refetchInterval: 1000,
         retry: false,
      },
   },
})

const StatusChecker: FC = ({ children }) => {
   const { isSuccess } = useQuery('api-status', () => request('api', { method: 'HEAD' }))
   return (
      <>
         {isSuccess || <Banner>Offline</Banner>}
         {children}
      </>
   )
}

ReactDOM.render(
   <React.StrictMode>
      <BrowserRouter>
         <ThemeProvider theme={dark}>
            <Global />
            <QueryClientProvider client={client}>
               <StatusChecker>
                  <SessionProvider>
                     <DialogProvider>
                        <App />
                     </DialogProvider>
                  </SessionProvider>
               </StatusChecker>
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
