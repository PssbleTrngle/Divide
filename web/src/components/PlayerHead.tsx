import { Buffer } from 'buffer'
import { HTMLAttributes, useMemo, VFC } from 'react'
import { useQuery } from 'react-query'
import styled, { css } from 'styled-components'
import { request } from '../hooks/useApi'
import useSession, { Player } from '../hooks/useSession'

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
   const url = `http://localhost:8001/https://sessionserver.mojang.com/session/minecraft/profile/${uuid.replaceAll(
      '-',
      ''
   )}`
   return request<PlayerData>(url)
}

const PlayerHead: VFC<
   Pick<Player, 'uuid'> &
      Partial<Player> &
      HTMLAttributes<HTMLDivElement> & {
         size?: number
         highlight?: boolean
      }
> = ({ uuid, name, size = 1, highlight, ...props }) => {
   const { player } = useSession()
   const yourself = useMemo(() => player?.uuid == uuid, [uuid, player])

   const { data } = useQuery(['skin', uuid], () => fetchData(uuid), { refetchInterval: false })
   const texture = useMemo(() => {
      const base = data?.properties.find(p => p.name === 'textures')?.value
      if (!base) return
      return JSON.parse(Buffer.from(base, 'base64').toString()) as TextureData
   }, [data])

   const title = useMemo(() => {
      if (yourself) return `${name} (you)`
      else return name
   }, [name, yourself])

   return (
      <Head
         {...props}
         data-tip={title}
         highlight={highlight ?? yourself}
         size={`${100 * size}px`}
         src={texture?.textures?.SKIN?.url}
      />
   )
}

const borderSize = '2px'
const Head = styled.div<{ src?: string; size: string; highlight?: boolean }>`
   background: #0001;
   background-image: url('${p => p.src}');
   height: ${p => p.size};
   width: ${p => p.size};
   background-size: calc(${p => p.size} * 8);
   background-position: calc(${p => p.size} * -1 - ${borderSize}) calc(${p => p.size} * -1 - ${borderSize});
   background-repeat: no-repeat;
   image-rendering: pixelated;

   border: ${borderSize} solid transparent;

   ${p =>
      p.highlight &&
      css`
         border-color: ${p.theme.primary};
      `}
`

export default PlayerHead
