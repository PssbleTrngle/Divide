import { ChangeEvent, useReducer, VFC } from 'react'
import styled from 'styled-components'
import Button from '../../components/Button'
import ErrorField from '../../components/ErrorField'
import Page from '../../components/Page'
import { Title } from '../../components/Text'
import useSubmit from '../../hooks/useSubmit'

const Upload: VFC = () => {
   const [body, setFiles] = useReducer((_: FormData | undefined, e: ChangeEvent<HTMLInputElement>) => {
      const { files } = e.target
      if (!files) return undefined

      const data = new FormData()
      for (let i = 0; i < files.length; i++) {
         const file: File = files[i]
         data.append(`file-${i}`, file, file.name)
      }

      return data
   }, undefined)

   const upload = useSubmit('game', { body })

   return (
      <Page>
         <Title>Upload new Game</Title>

         <form onSubmit={upload.send}>
            <ErrorField error={upload.error} />
            <Files htmlFor='files'>Select Files</Files>
            <input hidden id='files' type='file' multiple accept='.log' onChange={setFiles} />
            <Button>Upload</Button>
         </form>
      </Page>
   )
}

const Files = styled.label`
   padding: 0.5em;
   background: ${p => p.theme.accent};
   border-radius: 0.5em;
   cursor: pointer;
`

export default Upload
