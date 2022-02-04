import { Buffer } from 'buffer'
import { lighten } from 'polished'
import { HTMLAttributes, useMemo, VFC } from 'react'
import { useQuery } from 'react-query'
import styled, { css } from 'styled-components'
import { request } from '../hooks/useApi'
import useSession from '../hooks/useSession'
import useTooltip from '../hooks/useTooltip'
import { Player } from '../models/player'
import { loading, pseudo } from '../styles/mixins'

interface PlayerData {
   name: string
   id: string
   properties: Array<{
      name: string
      value: string
   }>
}

interface TextureData {
   textures: Record<
      'SKIN' | 'CAPE',
      {
         url: string
      }
   >
}

async function fetchData(uuid: string) {
   const { hostname, protocol } = window.location
   const url = `${protocol}//${hostname}/mojang/session/minecraft/profile/${uuid.replaceAll('-', '')}`
   return request<PlayerData>(url, {
      headers: {
         'X-Requested-With': 'XMLHttpRequest',
      },
   })
}

const PlayerHead: VFC<
   Pick<Player, 'uuid'> &
      Partial<Player> &
      HTMLAttributes<HTMLDivElement> & {
         size?: string | number
         highlight?: boolean | string
      }
> = ({ uuid, name, size = 1, highlight, ...props }) => {
   const { player } = useSession()
   const yourself = useMemo(() => player?.uuid == uuid, [uuid, player])

   const { data, isLoading } = useQuery(['skin', uuid], () => fetchData(uuid), { refetchInterval: false })

   const texture = useMemo(() => {
      const base = data?.properties.find(p => p.name === 'textures')?.value
      if (!base) return
      return JSON.parse(Buffer.from(base, 'base64').toString()) as TextureData
   }, [data])

   const title = useMemo(() => {
      if (yourself) return `${name} (you)`
      else return name
   }, [name, yourself])

   useTooltip()

   return (
      <Head
         {...props}
         data-tip={title}
         highlight={highlight ?? yourself}
         size={typeof size === 'number' ? `${100 * size}px` : size}
         animate={isLoading}
         src={texture?.textures?.SKIN?.url}
      />
   )
}

const Head = styled.div<{ src?: string; size: string; highlight?: string | boolean; animate?: boolean }>`
   ${loading};

   height: ${p => p.size};
   width: ${p => p.size};

   outline: max(calc(${p => p.size} / 20), 2px) solid transparent;
   transition: outline-color 0.2s ease-out;

   &::after {
      ${pseudo};
      background-image: url('${p => p.src}');
      background-size: calc(${p => p.size} * 8);
      background-position: calc(${p => p.size} * -1) calc(${p => p.size} * -1);
      background-repeat: no-repeat;
      image-rendering: pixelated;
   }

   ${p =>
      p.highlight &&
      css`
         outline-color: ${typeof p.highlight === 'string' ? p.highlight : p.theme.primary};

         &:hover {
            outline-color: ${lighten(0.05, typeof p.highlight === 'string' ? p.highlight : p.theme.primary)};
         }
      `}
`

export default PlayerHead
