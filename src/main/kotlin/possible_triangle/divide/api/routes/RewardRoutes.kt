package possible_triangle.divide.api.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import possible_triangle.divide.data.Reward

fun Route.rewardRouting() {
    route("/reward") {
        get {
            call.respond(Reward.values())
        }
    }
}