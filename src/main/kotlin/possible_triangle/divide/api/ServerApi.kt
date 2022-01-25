package possible_triangle.divide.api

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.PaymentRequired
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import org.slf4j.event.Level
import possible_triangle.divide.Config
import possible_triangle.divide.GameData
import possible_triangle.divide.bounty.Bounty
import possible_triangle.divide.crates.Order
import possible_triangle.divide.data.EventPlayer
import possible_triangle.divide.data.ReloadedResource
import possible_triangle.divide.events.Eras
import possible_triangle.divide.info.Scores
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.missions.Mission
import possible_triangle.divide.missions.MissionEvent
import possible_triangle.divide.reward.Action
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext
import java.util.*

@Serializable
private data class ApiError(val message: String?, val stack: List<String>?)

private class ApiException(message: String? = null, val status: HttpStatusCode = InternalServerError) :
    Exception(message)

@Serializable
private data class Resource<T>(val id: String, val value: T)

@ExperimentalSerializationApi
object ServerApi {

    private val ISSUER = "${Config.CONFIG.api.host}:${Config.CONFIG.api.port}"
    private val AUDIENCE = "$ISSUER/api"
    private const val REALM = "Access to 'api'"

    private var webServer: NettyApplicationEngine? = null

    fun createToken(player: ServerPlayer): String {
        return JWT.create()
            .withClaim("uuid", player.stringUUID)
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withExpiresAt(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
            .sign(Algorithm.HMAC256(Config.CONFIG.api.secret))
    }

    fun start(server: MinecraftServer) {

        stop()

        fun <T, R> Route.resource(path: String, resource: ReloadedResource<T>, transform: (T) -> R) {
            route(path) {
                get {
                    call.respond(resource.values.map { Resource(resource.idOf(it), transform(it)) })
                }
            }
        }

        fun <T> Route.resource(path: String, resource: ReloadedResource<T>) {
            resource(path, resource) { it }
        }

        fun PipelineContext<Unit, ApplicationCall>.getPlayer(): ServerPlayer {
            val data = call.principal<JWTPrincipal>() ?: throw ApiException("not logged in", Unauthorized)
            val uuid = UUID.fromString(data.payload.getClaim("uuid").asString())
            val player = server.playerList.getPlayer(uuid) ?: throw ApiException("player not found", Unauthorized)
            if (!Teams.isPlayer(player)) throw ApiException("not playing", Forbidden)
            return player
        }

        fun PipelineContext<Unit, ApplicationCall>.getTeam(): PlayerTeam {
            val player = getPlayer()
            return Teams.teamOf(player) ?: throw ApiException("not playing", Forbidden)
        }

        webServer = embeddedServer(Netty, port = Config.CONFIG.api.port) {

            install(CallLogging) { level = Level.INFO }
            install(ContentNegotiation) { json() }

            install(StatusPages) {
                exception<Throwable> { error ->
                    val status = if (error is ApiException) error.status else InternalServerError
                    call.respond(
                        status,
                        ApiError(error.message, error.stackTrace.map { it.toString() })
                    )
                }
            }

            install(Authentication) {
                jwt("auth-jwt") {
                    realm = REALM
                    verifier(
                        JWT.require(Algorithm.HMAC256(Config.CONFIG.api.secret))
                            .withIssuer(ISSUER)
                            .withAudience(AUDIENCE)
                            .build()
                    )

                    validate {
                        try {
                            UUID.fromString(it.payload.getClaim("uuid").asString())
                            JWTPrincipal(it.payload)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }
                }
            }

            routing {
                route("/api") {

                    route("/ping") {
                        get {
                            call.respond("pong")
                        }
                    }

                    authenticate("auth-jwt", optional = true) {
                        route("/status") {

                            @Serializable
                            data class GameStatus(
                                val peaceUntil: Int?,
                                val points: Int,
                                val mission: Mission?,
                                val paused: Boolean,
                                val started: Boolean
                            )

                            get {
                                call.respond(
                                    GameStatus(
                                        peaceUntil = if (Eras.isPeace(server)) Eras.remaining(server) else null,
                                        points = Points.get(server, getTeam()),
                                        mission = MissionEvent.ACTIVE[server]?.mission,
                                        paused = GameData.DATA[server].paused,
                                        started = GameData.DATA[server].started,
                                    )
                                )
                            }
                        }

                        route("/team") {
                            get {
                                val players = Teams.players(server, getTeam()).map { EventPlayer.of(it) }
                                call.respond(players)
                            }
                        }

                        route("/ranks") {
                            get {
                                val ranks = Scores.getRanks(server).mapKeys { EventPlayer.of(it.key).name }
                                call.respond(ranks)
                            }
                        }

                        route("/opponents") {
                            get {
                                val players = Teams.players(server)
                                    .filter { getTeam().name != it.team?.name }
                                    .map { EventPlayer.of(it) }
                                call.respond(players)
                            }
                        }

                        route("/auth") {
                            get {
                                call.respond(EventPlayer.of(getPlayer()))
                            }
                        }

                        route("/buy/{id}") {

                            @Serializable
                            data class Params(val target: String?)

                            post {
                                val reward = Reward[call.parameters["id"] ?: throw ApiException(status = BadRequest)]
                                    ?: throw ApiException(status = NotFound)

                                val params = call.receive<Params>()

                                fun <R, T> parseFor(action: Action<R, T>): HttpStatusCode {
                                    val target = action.target.fromString(params.target ?: "")

                                    val ctx = RewardContext<R, T>(getTeam(), server, getPlayer().uuid, target, reward)
                                    return ctx.ifComplete { _, _ ->
                                        if (Reward.buy(ctx)) OK else PaymentRequired
                                    } ?: BadRequest
                                }

                                call.respond(parseFor(reward.action))
                            }

                        }

                        route("/order/{id}") {

                            @Serializable
                            data class Params(val amount: Int = 1)

                            post {
                                val order = Order[call.parameters["id"] ?: throw ApiException(status = BadRequest)]
                                    ?: throw ApiException(status = NotFound)

                                val params = call.receive<Params>()
                                call.respond(
                                    if (order.order(
                                            getPlayer(),
                                            getTeam(),
                                            params.amount
                                        )
                                    ) OK else PaymentRequired
                                )
                            }
                        }

                        route("/events") {
                            get {
                                if (Teams.isAdmin(getPlayer())) call.respond(
                                    EventLogger.lines(server).joinToString(
                                        prefix = "[",
                                        postfix = "]"
                                    )
                                )
                                else call.respond(Unauthorized)
                            }
                        }

                        @Serializable
                        data class ExtendedReward(val reward: Reward, val target: String)

                        resource("reward", Reward) { ExtendedReward(it, it.action.target.id) }
                        resource("bounty", Bounty)
                        resource("order", Order)

                    }
                }
            }
        }

        Thread { webServer?.start(wait = true) }.start()
    }

    fun stop() {
        webServer?.stop(1000L, 4000L)
    }

}