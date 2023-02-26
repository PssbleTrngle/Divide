package possible_triangle.divide.logic

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.phys.AABB
import net.minecraft.world.scores.PlayerTeam
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.data.ModSavedData
import possible_triangle.divide.data.Util
import possible_triangle.divide.data.Util.persistentData
import possible_triangle.divide.extensions.*
import possible_triangle.divide.logic.Teams.participantTeam
import possible_triangle.divide.logic.Teams.teammates
import possible_triangle.divide.reward.Reward
import possible_triangle.divide.reward.actions.TeamBuff

object Bases {

    const val COMPASS_TAG = "${DivideMod.ID}:base_compass"
    private const val IN_BASE_TAG = "${DivideMod.ID}:in_base"

    fun updateBaseState(player: ServerPlayer) {
        val data = player.persistentData()
        val lastState = data.getBoolean(IN_BASE_TAG)
        val currentState = player.isInBase()
        if (lastState != currentState) {
            data.putBoolean(IN_BASE_TAG, currentState)
        }
    }

    fun boostCrops(world: ServerLevel) {
        if (world.time() % 200 != 0L) return

        Data[world.server]
            .filterKeys { TeamBuff.isBuffed(world.server, it, Reward.BUFF_CROPS) }
            .filterValues { (_, dim) -> dim == world.dimension() }
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

    fun removeBase(team: PlayerTeam, server: MinecraftServer) {
        Data.modify(server) {
            remove(team)
        }
    }

    fun setBase(player: ServerPlayer) {
        val team = player.participantTeam() ?: return
        val oldBase = Data[player.server][team]

        if (oldBase != null) {
            player.server.getLevel(oldBase.second)?.setBlock(
                oldBase.first.atY(player.level.minBuildHeight),
                Blocks.BEDROCK.defaultBlockState(), 2
            )
        }

        player.level.setBlock(
            player.blockPosition().atY(player.level.maxBuildHeight),
            Blocks.LODESTONE.defaultBlockState(), 2
        )

        Data.modify(player.server) {
            set(team, player.blockPosition() to player.level.dimension())
        }

        val compass = createCompass(player) ?: return
        player.teammates().forEach { teammate ->
            val compassSlot = teammate.inventory.items().indexOfFirst { it.tag?.getBoolean(COMPASS_TAG) == true }
            if (compassSlot >= 0) teammate.inventory.setItem(compassSlot, compass)
            else if (oldBase == null) teammate.addItem(compass)
        }
    }

    fun getBase(server: MinecraftServer, team: PlayerTeam): Pair<BlockPos, ResourceKey<Level>>? {
        return Data[server][team]
    }

    fun baseBox(pos: BlockPos) = AABB(pos).inflate(Config.CONFIG.bases.radius)

    fun createCompass(player: ServerPlayer): ItemStack? {
        val team = player.participantTeam() ?: return null
        val (pos, dimension) = getBase(player.server, team) ?: return null

        val stack = ItemStack(Items.COMPASS)
        stack.orCreateTag.putBoolean(COMPASS_TAG, true)
        stack.enchant(Enchantments.VANISHING_CURSE, 1)
        stack.setHoverName(Component.literal("Base Compass").noItalic())

        stack.orCreateTag.put("LodestonePos", pos.atY(player.level.minBuildHeight).toNbt())
        Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, dimension).result().ifPresent {
            stack.orCreateTag.put("LodestoneDimension", it)
        }

        stack.orCreateTag.putBoolean("LodestoneTracked", true)

        return stack
    }

    fun ServerPlayer.isInBase(useTag: Boolean = false): Boolean {
        val persistent = persistentData()
        if (useTag && persistent.contains(IN_BASE_TAG)) return persistent.getBoolean(IN_BASE_TAG)
        val team = participantTeam() ?: return false
        val (pos, dimension) = getBase(server, team) ?: return false
        if (dimension != level.dimension()) return false
        return baseBox(pos).contains(position())
    }

    private val Data = object : ModSavedData<MutableMap<PlayerTeam, Pair<BlockPos, ResourceKey<Level>>>>("bases") {
        override fun save(nbt: CompoundTag, value: MutableMap<PlayerTeam, Pair<BlockPos, ResourceKey<Level>>>) {
            value.forEach { (team, pos) ->
                pos.first.toNbt().apply {
                    Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, pos.second).result().ifPresent {
                        put("dimension", it)
                        nbt.put(team.name, this)
                    }
                }
            }
        }

        override fun load(
            nbt: CompoundTag,
            server: MinecraftServer,
        ): MutableMap<PlayerTeam, Pair<BlockPos, ResourceKey<Level>>> {
            return nbt.allKeys.mapNotNull { server.scoreboard.getPlayerTeam(it) }.associateWith { team ->
                val tag = nbt.getCompound(team.name)
                val pos = tag.toBlockPos()
                val dimension = Level.RESOURCE_KEY_CODEC.decode(NbtOps.INSTANCE, tag.get("dimension")).result()
                dimension.map { pos to it.first }
            }.filterValues { it.isPresent }.mapValues { it.value.get() }.toMutableMap()
        }

        override fun default(): MutableMap<PlayerTeam, Pair<BlockPos, ResourceKey<Level>>> {
            return mutableMapOf()
        }
    }

}