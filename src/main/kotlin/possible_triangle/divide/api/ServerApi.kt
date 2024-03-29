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
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import org.slf4j.event.Level
import possible_triangle.divide.Config
import possible_triangle.divide.GameData
import possible_triangle.divide.bounty.Bounty
import possible_triangle.divide.crates.CrateScheduler
import possible_triangle.divide.crates.Order
import possible_triangle.divide.data.EventTarget
import possible_triangle.divide.data.ReloadedResource
import possible_triangle.divide.events.Eras
import possible_triangle.divide.info.Scores
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logging.LoggedEvent
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.missions.MissionEvent
import possible_triangle.divide.missions.MissionEvent.MissionStatus
import possible_triangle.divide.reward.ActionTarget
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext
import java.util.*
import java.util.concurrent.Executors

@Serializable
private data class ApiError(val message: String?, val stack: List<String>?)

@Serializable
private data class Resource<T>(val id: String, val value: T)

@ExperimentalSerializationApi
object ServerApi {

    private val ISSUER = "${Config.CONFIG.api.host}:${Config.CONFIG.api.port}"
    private val AUDIENCE = "$ISSUER/api"
    private const val REALM = "Access to 'api'"

    private var webServer: NettyApplicationEngine? = null
    private var server: MinecraftServer? = null

    private val socketSessions = Collections.synchronizedSet<DefaultWebSocketServerSession>(LinkedHashSet())

    fun createToken(player: ServerPlayer): String {
        return JWT.create()
            .withClaim("uuid", player.stringUUID)
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withExpiresAt(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
            .sign(Algorithm.HMAC256(Config.CONFIG.api.secret))
    }

    private fun authenticatedPlayer(call: ApplicationCall, server: MinecraftServer): ServerPlayer {
        val data = call.principal<JWTPrincipal>() ?: throw ApiException("not logged in", Unauthorized)
        val uuid = UUID.fromString(data.payload.getClaim("uuid").asString())
        val player = server.playerList.getPlayer(uuid) ?: throw ApiException("player not found", Unauthorized)
        if (!Teams.isPlayer(player)) throw ApiException("not playing", Forbidden)
        return player
    }

    private val socketDispatcher = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    fun <T> notify(
        event: LoggedEvent<T>,
        serializer: KSerializer<T>,
        predicate: (ServerPlayer?) -> Boolean,
    ) {
        val server = server ?: return
        if (!Config.CONFIG.api.enabled) return

        val serialized = Json.encodeToString(LoggedEvent.serializer(serializer), event)
        socketSessions.filter {
            val player = try {
                authenticatedPlayer(it.call, server)
            } catch (e: ApiException) {
                null
            }
            predicate(player)
        }.forEach {
            socketDispatcher.launch(Dispatchers.IO) {
                it.send(serialized)
            }
        }
    }

    fun start(server: MinecraftServer) {

        stop()

        this.server = server

        fun ApplicationCall.player(): ServerPlayer {
            return authenticatedPlayer(this, server)
        }

        fun ApplicationCall.optionalPlayer(): ServerPlayer? {
            return try {
                player()
            } catch (e: ApiException) {
                null
            }
        }

        fun ApplicationCall.optionalTeam(): PlayerTeam? {
            val player = optionalPlayer() ?: return null
            return Teams.teamOf(player)
        }

        fun ApplicationCall.team(): PlayerTeam {
            return optionalTeam() ?: throw ApiException("not playing", Forbidden)
        }

        fun <T> Route.resource(path: String, resource: ReloadedResource<T>, additionalRoutes: Route.() -> Unit = {}) {
            route(path) {
                route("/{id}") {
                    get {
                        val entry = resource[call.parameters["id"] ?: throw ApiException(status = BadRequest)]
                            ?.takeIf { resource.isVisible(it, call.optionalTeam(), server) }
                            ?.let { Resource<T>(resource.idOf(it), it) }
                            ?: throw ApiException(status = NotFound)
                        call.respond(Json.encodeToString(Resource.serializer(resource.serializer()), entry))
                    }
                }

                get {
                    val team = call.optionalTeam()
                    val list = resource.values
                        .filter { resource.isVisible(it, team, server) }
                        .map { Resource(resource.idOf(it), it) }
                    call.respond(Json.encodeToString(ListSerializer(Resource.serializer(resource.serializer())), list))
                }

                additionalRoutes(this)
            }
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

            install(WebSockets)

            routing {

                route("/api") {
                    head {
                        call.respond(OK)
                    }

                    authenticate("auth-jwt", optional = true) {

                        @Serializable
                        data class SocketEvent(val message: String?)

                        webSocket {

                            suspend fun sendEvent(event: SocketEvent) {
                                val serialized = Json.encodeToString(SocketEvent.serializer(), event)
                                send(serialized)
                            }

                            try {
                                socketSessions.add(this)
                                sendEvent(SocketEvent("connect"))

                                for (frame in incoming) {
                                    sendEvent(SocketEvent("pong"))
                                }

                            } catch (e: ClosedReceiveChannelException) {
                                socketSessions.remove(this)
                            }
                        }

                        resource("reward", Reward)
                        resource("bounty", Bounty)
                        resource("order", Order) {
                            route("/bought") {

                                @Serializable
                                data class TeamOrder(val order: Order, val amount: Int)

                                get {
                                    val team = call.team()
                                    call.respond(CrateScheduler.getOrders(server, team)
                                        .map { TeamOrder(it.key, it.value) })
                                }
                            }
                        }

                        route("/status") {

                            @Serializable
                            data class GameStatus(
                                val peaceUntil: Int?,
                                val mission: MissionStatus?,
                                val paused: Boolean,
                                val started: Boolean,
                            )

                            get {
                                call.respond(
                                    GameStatus(
                                        peaceUntil = if (Eras.isPeace(server)) Eras.remaining(server) else null,
                                        mission = MissionEvent.status(server, call.optionalPlayer()),
                                        paused = GameData.DATA[server].paused,
                                        started = GameData.DATA[server].started,
                                    )
                                )
                            }
                        }

                        route("/points") {
                            get {
                                call.respond(Points.get(server, call.team()))
                            }
                        }

                        route("/player") {
                            get {
                                val team = call.optionalTeam()
                                val params = call.request.queryParameters
                                val players = Teams.players(server)
                                    .filter { params["opponent"] != "true" || it.team != team }
                                    .filter { params["teammate"] != "true" || it.team == team }
                                    .filter { params["team"] == null || it.team?.name == params["team"] }
                                    .map { EventTarget.of(it) }
                                call.respond(players)
                            }
                        }

                        route("/team") {
                            get {
                                val team = call.optionalTeam()
                                val params = call.request.queryParameters
                                val teams = Teams.teams(server)
                                    .filter { params["opponent"] != "true" || it != team }
                                call.respond(teams.map { EventTarget.of(it) })
                            }
                        }

                        @Serializable
                        data class RankPosition(val rank: Int, val team: EventTarget)

                        route("/ranks") {
                            get {
                                val ranks = Scores.getRanks(server).map {
                                    RankPosition(it.value, EventTarget.of(it.key))
                                }
                                call.respond(ranks)
                            }
                        }

                        route("/auth") {
                            get {
                                call.respond(EventTarget.of(call.player()))
                            }
                        }

                        route("/buy/{id}") {

                            @Serializable
                            data class Params(val target: String?)

                            post {
                                val reward = Reward[call.parameters["id"] ?: throw ApiException(status = BadRequest)]
                                    ?.takeIf { Reward.isVisible(it, call.team(), server) }
                                    ?: throw ApiException(status = NotFound)

                                val params = call.receive<Params>()

                                fun <T> parseFor(targetType: ActionTarget<T>): HttpStatusCode {
                                    val target = targetType.fromString(params.target ?: "")

                                    val ctx =
                                        RewardContext(
                                            call.team(),
                                            server,
                                            call.player().uuid,
                                            target,
                                            reward,
                                            targetType
                                        )

                                    return ctx.player?.let { _ ->
                                        if (Reward.buy(ctx)) OK else PaymentRequired
                                    } ?: BadRequest
                                }

                                call.respond(parseFor(reward.target))
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
                                    if (order.order(call.player(), params.amount)) OK else PaymentRequired
                                )
                            }
                        }

                        route("/events") {
                            get {
                                call.respond(
                                    EventLogger.lines(call.optionalPlayer(), Config.CONFIG.api.ignoreEventPermission)
                                        .joinToString(prefix = "[", postfix = "]")
                                )
                            }

                            route("/{type}") {
                                get {
                                    val type = call.parameters["type"] ?: throw ApiException(status = BadRequest)
                                    call.respond(
                                        EventLogger.lines(
                                            call.optionalPlayer(),
                                            Config.CONFIG.api.ignoreEventPermission,
                                            type,
                                        ).joinToString(prefix = "[", postfix = "]")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Thread { webServer?.start(wait = true) }.start()
    }

    fun stop() {
        webServer?.stop(1000L, 4000L)
        server = null
    }

}