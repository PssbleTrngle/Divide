import styled from 'styled-components'

type Props = ({ perRow: number; size?: never } | { perRow?: never; size: string }) & { center?: boolean }

const PlayerList = styled.section<Props>`
   display: grid;
   grid-template-columns: repeat(${p => p.perRow ?? 'auto-fit'}, ${p => p.size ?? '1fr'});
   grid-auto-flow: column;

   justify-content: ${p => (p.center ? 'center' : null)};

   gap: 1em;
`

export default PlayerList
