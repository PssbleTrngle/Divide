package possible_triangle.divide.data

import kotlinx.serialization.Serializable

fun first(first: Int, then: Int): (times: Int) -> Int {
    return { if (it > 0) then else first }
}

fun increasing(start: Int, by: Int): (times: Int) -> Int {
    return { start + it * by }
}

@Serializable
enum class Bounty(val display: String, val amount: (times: Int) -> Int) {

    PLAYER_KILL("Kill a Player", { 100 }),

    ADVANCEMENT("Unlock an advancement", increasing(20, 10)),

    MINED_IRON("Mine a iron ore", first(20, 1)),
    MINED_DIAMOND("Mine a diamond ore", first(200, 10));

}