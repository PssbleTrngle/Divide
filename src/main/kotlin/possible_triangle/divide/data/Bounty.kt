package possible_triangle.divide.data

import kotlinx.serialization.Serializable

fun first(first: Int, then: Int = 0): (times: Int) -> Int {
    return { if (it > 0) then else first }
}

fun increasing(start: Int, by: Int): (times: Int) -> Int {
    return { start + it * by }
}

@Serializable
enum class Bounty(val display: String, val amount: (times: Int) -> Int) {

    PLAYER_KILL("Kill a Player", { 100 }),
    BLOWN_UP("Blow a player up", { 200 }),
    ADVANCEMENT("Unlock an advancement", increasing(20, 10)),

    SOLD_HEART("Sold a heart", { 200 }),

    MINED_COAL("Mine a coal ore", first(20)),
    MINED_IRON("Mine a iron ore", first(20, 1)),
    MINED_GOLD("Mine a gold ore", first(20, 1)),
    MINED_DIAMOND("Mine a diamond ore", first(200, 10)),
    MINED_EMERALD("Mine a emerald ore", first(200, 10)),
    MINED_NETHERITE("Mine ancient debris", first(300, 20));

}