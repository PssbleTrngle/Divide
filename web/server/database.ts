import { MongoClient, colors } from "./deps.ts"

const client = new MongoClient()

const uri = Deno.env.get("MONGO_URI") || "mongodb://localhost:27017"
console.log(`Connecting to database at ${colors.underline(uri)}`)
await client.connect(uri)

export default client.database(Deno.env.get("MONGO_DB") || "divide")
