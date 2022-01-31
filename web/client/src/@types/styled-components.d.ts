import 'styled-components';

declare module 'styled-components' {
   interface DefaultTheme {
      bg: string
      primary: string
      accent: string
      text: string
      error: string
      warning: string
      ok: string
   }
}
