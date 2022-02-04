import { MongoClient, colors } from "./deps.ts"
import config from './config.ts'

const client = new MongoClient()

console.log(`Connecting to database at ${colors.underline(config.mongo.uri)}`)
await client.connect(config.mongo.uri)

export default client.database(config.mongo.db)
