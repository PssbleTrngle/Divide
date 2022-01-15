import { Buffer } from 'buffer'
import { useMemo, VFC } from 'react'
import { useQuery } from 'react-query'
import styled from 'styled-components'
import { request } from '../hooks/useApi'
import { Player } from '../hooks/useSession'

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

const PlayerHead: VFC<Pick<Player, 'uuid'> & Partial<Player>> = ({ uuid, name }) => {
   const { data } = useQuery(['skin', uuid], () => fetchData(uuid), { refetchInterval: false })
   const texture = useMemo(() => {
      const base = data?.properties.find(p => p.name === 'textures')?.value
      if (!base) return
      return JSON.parse(Buffer.from(base, 'base64').toString()) as TextureData
   }, [data])

   return <Head size='100px' src={texture?.textures?.SKIN?.url} />
}

const Head = styled.div<{ src?: string; size: string }>`
   background: #0001;
   background-image: url('${p => p.src}');
   height: ${p => p.size};
   width: ${p => p.size};
   background-size: calc(${p => p.size} * 8);
   background-position: calc(${p => p.size} * -1) calc(${p => p.size} * -1);
   background-repeat: no-repeat;
   image-rendering: pixelated;
`

export default PlayerHead
