package possible_triangle.divide

import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import possible_triangle.divide.crates.CrateLoot
import possible_triangle.divide.data.ReloadedResource

@Mod(DivideMod.ID)
object DivideMod {
    const val ID = "divide"

    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        LOGGER.info("Divide booting")

        ReloadedResource.register(CrateLoot)
    }

}