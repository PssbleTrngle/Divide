package possible_triangle.divide.logic

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtUtils
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextComponent
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
import net.minecraft.world.scores.Team
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import possible_triangle.divide.Config
import possible_triangle.divide.DivideMod
import possible_triangle.divide.reward.actions.TeamBuff
import possible_triangle.divide.data.ModSavedData
import possible_triangle.divide.data.Util
import possible_triangle.divide.reward.Reward
import java.util.*

@Mod.EventBusSubscriber
object Bases {

    const val COMPASS_TAG = "${DivideMod.ID}:base_compass"
    private const val IN_BASE_TAG = "${DivideMod.ID}:in_base"
    private val TICK_RANDOM = Random()

    @SubscribeEvent
    fun playerTick(event: TickEvent.PlayerTickEvent) {
        if (Util.shouldSkip(event, { it.player.level }, ticks = 1)) return

        val player = event.player
        if (player !is ServerPlayer) return

        val data = Util.persistentData(player)
        val lastState = data.getBoolean(IN_BASE_TAG)
        val currentState = isInBase(player)
        if (lastState != currentState) {
            data.putBoolean(IN_BASE_TAG, currentState)
        }
    }

    @SubscribeEvent
    fun worldTick(event: TickEvent.WorldTickEvent) {
        if (Util.shouldSkip(event, { event.world }, ticks = 200, onlyOverworld = false)) return
        val world = event.world as ServerLevel

        Data[world.server]
            .filterKeys { TeamBuff.isBuffed(world.server, it, Reward.BUFF_CROPS) }
            .filterValues { (_, dim) -> dim == world.dimension() }
            .forEach { (_, pos) ->
                if (world.isAreaLoaded(pos.first, 1)) {
                    val blocks = Util.blocksIn(baseBox(pos.first))
                    blocks.forEach {
                        val state = world.getBlockState(it)
                        if (state.block is CropBlock) {
                            (state.block as CropBlock).randomTick(state, world, it, TICK_RANDOM)
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

    fun setBase(player: ServerPlayer) {
        val team = player.team ?: return
        val oldBase = Data[player.server][team]

        if (oldBase != null) {
            player.server.getLevel(oldBase.second)?.setBlock(
                oldBase.first.atY(player.level.minBuildHeight),
                Blocks.BEDROCK.defaultBlockState(), 2
            )
        }

        player.level.setBlock(
            player.blockPosition().atY(player.level.minBuildHeight),
            Blocks.LODESTONE.defaultBlockState(), 2
        )

        Data.modify(player.server) {
            set(team, player.blockPosition() to player.level.dimension())
        }

        val compass = createCompass(player) ?: return
        Teams.teammates(player).forEach { teammate ->
            val compassSlot = teammate.inventory.items.indexOfFirst { it.tag?.getBoolean(COMPASS_TAG) == true }
            if (compassSlot >= 0) teammate.inventory.setItem(compassSlot, compass)
            else if (oldBase == null) teammate.addItem(compass)
        }
    }

    fun getBase(server: MinecraftServer, team: Team): Pair<BlockPos, ResourceKey<Level>>? {
        return Data[server][team]
    }

    fun baseBox(pos: BlockPos): AABB {
        return AABB(pos).inflate(Config.CONFIG.bases.radius)
    }

    fun createCompass(player: ServerPlayer): ItemStack? {
        val team = player.team ?: return null
        val (pos, dimension) = getBase(player.server, team) ?: return null

        val stack = ItemStack(Items.COMPASS)
        stack.orCreateTag.putBoolean(COMPASS_TAG, true)
        stack.enchant(Enchantments.VANISHING_CURSE, 1)
        stack.hoverName = TextComponent("Base Compass").withStyle(Style.EMPTY.withItalic(false))

        stack.orCreateTag.put("LodestonePos", NbtUtils.writeBlockPos(pos.atY(player.level.minBuildHeight)))
        Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, dimension).result().ifPresent {
            stack.orCreateTag.put("LodestoneDimension", it)
        }

        stack.orCreateTag.putBoolean("LodestoneTracked", true)

        return stack
    }

    fun isInBase(player: ServerPlayer, useTag: Boolean = false): Boolean {
        val persistent = Util.persistentData(player)
        if(useTag && persistent.contains(IN_BASE_TAG)) return persistent.getBoolean(IN_BASE_TAG)
        val team = player.team ?: return false
        val (pos, dimension) = getBase(player.server, team) ?: return false
        if (dimension != player.level.dimension()) return false
        return baseBox(pos).contains(player.position())
    }

    private val Data = object : ModSavedData<MutableMap<Team, Pair<BlockPos, ResourceKey<Level>>>>("bases") {
        override fun save(nbt: CompoundTag, value: MutableMap<Team, Pair<BlockPos, ResourceKey<Level>>>) {
            value.forEach { (team, pos) ->
                with(NbtUtils.writeBlockPos(pos.first)) {
                    Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, pos.second).result().ifPresent {
                        put("dimension", it)
                        nbt.put(team.name, this)
                    }
                }
            }
        }

        override fun load(
            nbt: CompoundTag,
            server: MinecraftServer
        ): MutableMap<Team, Pair<BlockPos, ResourceKey<Level>>> {
            return nbt.allKeys.mapNotNull { server.scoreboard.getPlayerTeam(it) }.associateWith { team ->
                val tag = nbt.getCompound(team.name)
                val pos = NbtUtils.readBlockPos(tag)
                val dimension = Level.RESOURCE_KEY_CODEC.decode(NbtOps.INSTANCE, tag.get("dimension")).result()
                dimension.map { pos to it.first }
            }.filterValues { it.isPresent }.mapValues { it.value.get() }.toMutableMap()
        }

        override fun default(): MutableMap<Team, Pair<BlockPos, ResourceKey<Level>>> {
            return mutableMapOf()
        }
    }

}