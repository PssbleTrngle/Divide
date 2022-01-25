import { VFC } from 'react'
import { Colored } from '../Text'
import { ActionEvent } from './types'

const ActionInfo: VFC<ActionEvent> = ({ reward, action }) => (
   <span>
      {action} <Colored>{reward}</Colored>
   </span>
)

export default ActionInfo
