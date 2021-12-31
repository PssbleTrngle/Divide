package possible_triangle.divide.api

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.Reward

@Mod.EventBusSubscriber
object Api {

    fun startServer() {
        embeddedServer(Netty, port = 8080) {
            routing {
                route("/reward") {
                    get {
                        call.respond(Reward.values())
                    }
                }
            }
        }.start(wait = true)
    }

    @SubscribeEvent
    fun onServerStart(event: ServerStartedEvent) {
        DivideMod.LOGGER.info("Server Started lololol")
        startServer()
    }

}