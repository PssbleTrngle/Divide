package possible_triangle.divide.api

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.PaymentRequired
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import org.slf4j.event.Level
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.bounty.Bounty
import possible_triangle.divide.crates.Order
import possible_triangle.divide.data.EventPlayer
import possible_triangle.divide.data.ReloadedResource
import possible_triangle.divide.events.Eras
import possible_triangle.divide.logging.EventLogger
import possible_triangle.divide.logic.Points
import possible_triangle.divide.logic.Scores
import possible_triangle.divide.logic.Teams
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.RewardContext
import java.util.*

object ServerApi {

    @Serializable
    data class GameStatus(val peaceUntil: Int?, val points: Int)

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

        fun <T> Route.resource(path: String, resource: ReloadedResource<T>) {
            val serializer = resource.serializer()
            route(path) {
                get {
                    val encoded = Json.encodeToString(ListSerializer(serializer), resource.values)
                    call.respond(encoded)
                }
            }
        }

        suspend fun PipelineContext<Unit, ApplicationCall>.auth(
            runnable: suspend PipelineContext<Unit, ApplicationCall>.(ServerPlayer, PlayerTeam) -> Unit
        ) {
            val data = call.principal<JWTPrincipal>() ?: return call.respond(Unauthorized)
            val uuid = UUID.fromString(data.payload.getClaim("uuid").asString())

            val player = server.playerList.getPlayer(uuid)
            val team = player?.team

            if (player == null) call.respond(Unauthorized, "Unauthorized")
            else if (!Teams.isPlayer(player) || team !is PlayerTeam) call.respond(Forbidden)
            else runnable(player, team)
        }

        webServer = embeddedServer(Netty, port = Config.CONFIG.api.port) {

            environment.monitor.subscribe(ApplicationStarted) {
                DivideMod.LOGGER.info("Webserver started")
                log.info("Webserver started")
            }

            environment.monitor.subscribe(ApplicationStopped) {
                DivideMod.LOGGER.info("Webserver stopped")
                log.info("Webserver stopped")
            }

            install(CallLogging) {
                level = Level.INFO
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
                            get {
                                auth { _, team ->
                                    call.respond(
                                        Json.encodeToString(
                                            GameStatus.serializer(),
                                            GameStatus(Eras.remaining(server), Points.get(server, team))
                                        )
                                    )
                                }
                            }
                        }

                        route("/team") {
                            get {
                                auth { _, team ->
                                    val players = Teams.players(server, team).map { EventPlayer.of(it) }
                                    call.respond(Json.encodeToString(ListSerializer(EventPlayer.serializer()), players))
                                }
                            }
                        }

                        route("/ranks") {
                            get {
                                val ranks = Scores.getRanks(server).mapKeys { it.key.name }
                                call.respond(
                                    Json.encodeToString(
                                        MapSerializer(String.serializer(), Int.serializer()),
                                        ranks
                                    )
                                )
                            }
                        }

                        route("/opponents") {
                            get {
                                auth { _, team ->
                                    val players = Teams.players(server)
                                        .filter { team.name != it.team?.name }
                                        .map { EventPlayer.of(it) }
                                    call.respond(Json.encodeToString(ListSerializer(EventPlayer.serializer()), players))
                                }
                            }
                        }

                        route("/auth") {
                            get {
                                auth { player, _ ->
                                    call.respond(Json.encodeToString(EventPlayer.serializer(), EventPlayer.of(player)))
                                }
                            }
                        }

                        route("/buy/{id}") {
                            post {
                                auth { player, team ->
                                    val reward = Reward[call.parameters["id"] ?: return@auth call.respond(BadRequest)]
                                        ?: return@auth call.respond(NotFound)

                                    val target = if (reward.requiresTarget) {
                                        val targetUUID = UUID.fromString(call.receiveParameters()["target"])
                                        server.playerList.getPlayer(targetUUID) ?: return@auth call.respond(BadRequest)
                                    } else player

                                    val ctx = RewardContext(team, server, player, target, reward)
                                    call.respond(if (reward.buy(ctx)) OK else PaymentRequired)
                                }
                            }
                        }

                        route("/order/{id}") {
                            post {
                                auth { player, team ->
                                    val order = Order[call.parameters["id"] ?: return@auth call.respond(BadRequest)]
                                        ?: return@auth call.respond(NotFound)
                                    val amount = call.receiveParameters()["amount"]?.toInt() ?: 1
                                    call.respond(if (order.order(player,team,amount)) OK else PaymentRequired)
                                }
                            }
                        }

                        route("/events") {
                            get {
                                auth { player, _ ->
                                    if (Teams.isAdmin(player)) call.respond(
                                        EventLogger.lines(server).joinToString(
                                            prefix = "[",
                                            postfix = "]"
                                        )
                                    )
                                    else call.respond(Unauthorized)
                                }
                            }
                        }

                        resource("reward", Reward)
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