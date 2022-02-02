import styled from 'styled-components'

type Props = { perRow: number; size?: never } | { perRow?: never; size: string }

const PlayerList = styled.section<Props>`
   display: grid;
   grid-template-columns: repeat(${p => p.perRow ?? 'auto-fill'}, ${p => p.size ?? '1fr'});
   grid-auto-flow: column;

   gap: 1em;
`

export default PlayerList
