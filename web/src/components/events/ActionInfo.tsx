import { VFC } from 'react'
import { Colored } from '../Text'
import { ActionEvent } from './types'

const ActionInfo: VFC<ActionEvent> = ({ reward, action }) => (
   <>
      {action} <Colored>{reward}</Colored>
   </>
)

export default ActionInfo
