import styled from 'styled-components'

const PlayerList = styled.section<{ perRow: number }>`
   display: grid;
   grid-template-columns: repeat(${p => p.perRow}, 1fr);
   gap: 5px;
`

export default PlayerList
