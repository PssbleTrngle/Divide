package possible_triangle.divide.logic

import net.minecraft.block.Blocks
import net.minecraft.block.CropBlock
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.RegistryKey
import net.minecraft.scoreboard.Team
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.world.World
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.ModSavedData
import possible_triangle.divide.data.Util
import possible_triangle.divide.data.Util.persistentData
import possible_triangle.divide.extensions.items
import possible_triangle.divide.logic.Teams.participantTeam
import possible_triangle.divide.logic.Teams.teammates
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.actions.TeamBuff

object Bases {

    const val COMPASS_TAG = "${DivideMod.ID}:base_compass"
    private const val IN_BASE_TAG = "${DivideMod.ID}:in_base"

    fun updateBaseState(player: ServerPlayerEntity) {
        val data = player.persistentData()
        val lastState = data.getBoolean(IN_BASE_TAG)
        val currentState = player.isInBase()
        if (lastState != currentState) {
            data.putBoolean(IN_BASE_TAG, currentState)
        }
    }

    fun boostCrops(world: ServerWorld) {
        if (world.time % 200 != 0L) return

        Data[world.server]
            .filterKeys { TeamBuff.isBuffed(world.server, it, Reward.BUFF_CROPS) }
            .filterValues { (_, dim) -> dim == world.registryKey }
            .forEach { (_, pos) ->
                if (world.isAreaLoaded(pos.first, 1)) {
                    val blocks = Util.blocksIn(baseBox(pos.first))
                    blocks.forEach {
                        val state = world.getBlockState(it)
                        if (state.block is CropBlock) {
                            (state.block as CropBlock).randomTick(state, world, it, world.random)
                        }
                    }
                }
            }
    }

    fun removeBase(team: Team, server: MinecraftServer) {
        Data.modify(server) {
            remove(team)
        }
    }

    fun setBase(player: ServerPlayerEntity) {
        val team = player.participantTeam() ?: return
        val oldBase = Data[player.server][team]

        if (oldBase != null) {
            player.server.getWorld(oldBase.second)?.setBlockState(
                oldBase.first.withY(player.world.bottomY),
                Blocks.BEDROCK.defaultState, 2
            )
        }

        player.world.setBlockState(
            player.blockPos.withY(player.world.bottomY),
            Blocks.LODESTONE.defaultState, 2
        )

        Data.modify(player.server) {
            set(team, player.blockPos to player.world.registryKey)
        }

        val compass = createCompass(player) ?: return
        player.teammates().forEach { teammate ->
            val compassSlot = teammate.inventory.items().indexOfFirst { it.nbt?.getBoolean(COMPASS_TAG) == true }
            if (compassSlot >= 0) teammate.inventory.setStack(compassSlot, compass)
            else if (oldBase == null) teammate.giveItemStack(compass)
        }
    }

    fun getBase(server: MinecraftServer, team: Team): Pair<BlockPos, RegistryKey<World>>? {
        return Data[server][team]
    }

    fun baseBox(pos: BlockPos): Box {
        return Box(pos).expand(Config.CONFIG.bases.radius)
    }

    fun createCompass(player: ServerPlayerEntity): ItemStack? {
        val team = player.participantTeam() ?: return null
        val (pos, dimension) = getBase(player.server, team) ?: return null

        val stack = ItemStack(Items.COMPASS)
        stack.orCreateNbt.putBoolean(COMPASS_TAG, true)
        stack.addEnchantment(Enchantments.VANISHING_CURSE, 1)
        stack.setCustomName(Text.literal("Base Compass").styled { it.withItalic(false) })

        stack.orCreateNbt.put("LodestonePos", NbtHelper.fromBlockPos(pos.withY(player.world.bottomY)))
        World.CODEC.encodeStart(NbtOps.INSTANCE, dimension).result().ifPresent {
            stack.orCreateNbt.put("LodestoneDimension", it)
        }

        stack.orCreateNbt.putBoolean("LodestoneTracked", true)

        return stack
    }

    fun ServerPlayerEntity.isInBase(useTag: Boolean = false): Boolean {
        val persistent = persistentData()
        if (useTag && persistent.contains(IN_BASE_TAG)) return persistent.getBoolean(IN_BASE_TAG)
        val team = participantTeam() ?: return false
        val (pos, dimension) = getBase(server, team) ?: return false
        if (dimension != world.dimensionKey) return false
        return baseBox(pos).contains(this.pos)
    }

    private val Data = object : ModSavedData<MutableMap<Team, Pair<BlockPos, RegistryKey<World>>>>("bases") {
        override fun save(nbt: NbtCompound, value: MutableMap<Team, Pair<BlockPos, RegistryKey<World>>>) {
            value.forEach { (team, pos) ->
                with(NbtHelper.fromBlockPos(pos.first)) {
                    World.CODEC.encodeStart(NbtOps.INSTANCE, pos.second).result().ifPresent {
                        put("dimension", it)
                        nbt.put(team.name, this)
                    }
                }
            }
        }

        override fun load(
            nbt: NbtCompound,
            server: MinecraftServer,
        ): MutableMap<Team, Pair<BlockPos, RegistryKey<World>>> {
            return nbt.keys.mapNotNull { server.scoreboard.getPlayerTeam(it) }.associateWith { team ->
                val tag = nbt.getCompound(team.name)
                val pos = NbtHelper.toBlockPos(tag)
                val dimension = World.CODEC.decode(NbtOps.INSTANCE, tag.get("dimension")).result()
                dimension.map { pos to it.first }
            }.filterValues { it.isPresent }.mapValues { it.value.get() }.toMutableMap()
        }

        override fun default(): MutableMap<Team, Pair<BlockPos, RegistryKey<World>>> {
            return mutableMapOf()
        }
    }

}