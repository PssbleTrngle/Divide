package possible_triangle.divide.data

import net.minecraft.nbt.IntTag
import net.minecraft.nbt.NumericTag

class PerTeamIntData(
    key: String,
    initial: Int = 0,
) : PerTeamData<Int,NumericTag>(key, initial, IntTag::valueOf, { it.asInt })