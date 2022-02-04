import { dotenv } from "./deps.ts"

dotenv.config({ export: true })

function require(key: string) {
   const value = Deno.env.get(key)
   if (value) return value
   throw new Error(`Missing env variable: ${key}`)
}

const key = await crypto.subtle.generateKey({ name: "HMAC", hash: "SHA-512" }, true, ["sign", "verify"])

export default {
   password: require("ADMIN_PASSWORD"),
   jwt: {
      secret: require("JWT_SECRET"),
      expiresIn: 1000 * 60 * 60 * 24,
      key,
   },
   mongo: {
      uri: Deno.env.get("MONGO_URI") ?? "mongodb://localhost:27017",
      db: Deno.env.get("MONGO_DB") ?? "divide",
   },
}
