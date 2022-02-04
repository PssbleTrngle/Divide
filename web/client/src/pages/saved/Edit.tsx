import { debounce } from 'lodash'
import { useMemo, useReducer, VFC } from 'react'
import { useQueryClient } from 'react-query'
import { useParams } from 'react-router-dom'
import Input from '../../components/Input'
import Link from '../../components/Link'
import Page from '../../components/Page'
import { Title } from '../../components/Text'
import useApi from '../../hooks/useApi'
import useLoading from '../../hooks/useLoading'
import useSubmit from '../../hooks/useSubmit'
import { Game } from '../../models/game'

const Edit: VFC = () => {
   const params = useParams()
   const query = useApi<Game<string>>(`game/${params.game}`)
   return useLoading(query, game => <Form {...game} />)
}

function useUpdate<T, K extends keyof T>(queryKey: string, initial: T, key: K) {
   const save = useSubmit(queryKey, { method: 'PUT' })
   const client = useQueryClient()

   // eslint-disable-next-line react-hooks/exhaustive-deps
   const debouncedSave = useMemo(() => debounce(save.send, 300), [queryKey])

   return useReducer((_: T[K], v: T[K]) => {
      debouncedSave({ [key]: v })
      client.setQueryData<T>(queryKey, data => ({ ...data, ...initial, [key]: v }))
      return v
   }, initial[key])
}

const Form: VFC<Game<string>> = game => {
   const [name, setName] = useUpdate(`game/${game._id}`, game, 'name')

   return (
      <Page>
         <Link to='./..'>Back</Link>
         <Title>Edit {game.name ?? game._id}</Title>
         <label htmlFor='name'>Name</label>
         <Input id='name' value={name} onChange={e => setName(e.target.value)} />
      </Page>
   )
}

export default Edit
