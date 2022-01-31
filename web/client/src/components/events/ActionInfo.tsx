import { VFC } from 'react'
import { ActionEvent } from '../../models/events'
import { Colored } from '../Text'

const ActionInfo: VFC<ActionEvent> = ({ reward, action }) => (
   <>
      {action} <Colored>{reward}</Colored>
   </>
)

export default ActionInfo
