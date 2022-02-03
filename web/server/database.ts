import { MongoClient } from "./deps.ts"

const client = new MongoClient()

await client.connect(Deno.env.get("MONGO_URI") || "mongodb://localhost:27017")

export default client.database(Deno.env.get("MONGO_DB") || "divide")
