import { useState, VFC } from 'react'
import useApi from '../../hooks/useApi'
import { Submit } from '../../hooks/useSubmit'
import { Player } from '../../models/player'
import PlayerHead from '../PlayerHead'
import PlayerList from '../PlayerList'
import TargetDialog from './TargetDialog'

const ChoosePlayer: VFC<Submit> = props => {
   const { data } = useApi<Player[]>('player?opponent=true')
   const [selected, setSelected] = useState<string>()

   return (
      <TargetDialog selected={selected} {...props}>
         {!!data?.length && (
            <PlayerList perRow={1}>
               {data?.map(player => (
                  <PlayerHead
                     key={player.uuid}
                     {...player}
                     highlight={selected === player.uuid}
                     size={0.5}
                     onClick={() => setSelected(player.uuid)}
                  />
               ))}
            </PlayerList>
         )}
      </TargetDialog>
   )
}

export default ChoosePlayer
