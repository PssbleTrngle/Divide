import { MongoClient } from "mongo"

const client = new MongoClient()

await client.connect("mongodb://localhost:27017")

export default client.database('divide')