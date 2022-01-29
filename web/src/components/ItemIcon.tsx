import { VFC } from 'react'
import styled from 'styled-components'
import { loading, pseudo } from '../styles/mixins'

const ItemIcon: VFC<{ item: string; size?: string }> = ({ item, size = '2em', ...props }) => (
   <Style {...props} size={size} src={`/img/${item}.png`} />
)

const Style = styled.img<{ size: string }>`
   height: ${p => p.size};
   width: ${p => p.size};

   image-rendering: pixelated;

   &::before {
      background: ${p => p.theme.bg};
   }

   ${loading};

   &::after {
      ${pseudo};
   }
`

export default ItemIcon
