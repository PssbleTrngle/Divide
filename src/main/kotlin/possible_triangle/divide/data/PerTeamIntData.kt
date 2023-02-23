package possible_triangle.divide.data

import net.minecraft.nbt.AbstractNbtNumber
import net.minecraft.nbt.NbtInt

class PerTeamIntData(
    key: String,
    initial: Int = 0,
) : PerTeamData<Int, AbstractNbtNumber>(key, initial, NbtInt::of, { it.intValue() })