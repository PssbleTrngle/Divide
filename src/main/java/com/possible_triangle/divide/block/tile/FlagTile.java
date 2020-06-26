package com.possible_triangle.divide.block.tile;

import com.possible_triangle.divide.Content;
import com.possible_triangle.divide.block.Flag;
import com.possible_triangle.divide.data.PlayerData;
import com.possible_triangle.divide.data.TeamSavedData;
import com.possible_triangle.divide.data.ChunkProtection;
import com.possible_triangle.divide.data.ChunkSavedData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class FlagTile extends BaseTile implements ITickableTileEntity {

    private static final int STOLEN_PROT = 20 * 10;

    private String team;
    private TextFormatting color;
    private int rank = 0;
    private int protection = 0;

    public boolean isProtected() {
        return this.protection > 0;
    }

    @Override
    public void tick() {
        if (protection > 0) setProtection(protection - 1);
        getServerWorld().ifPresent(world -> {

            if (this.isProtected() && protection % 3 == 0) {
                double x = Math.random() + this.getPos().getX();
                double y = Math.random() * 2 + this.getPos().getY();
                double z = Math.random() + this.getPos().getZ();
                world.spawnParticle(ParticleTypes.ENCHANTED_HIT, x, y, z, 1, 0, 0, 0, 0);
            }

        });
    }

    public int getProtection() {
        return this.protection;
    }

    public int getRank() {
        return this.rank;
    }

    public void setProtection(int protection) {
        this.protection = Math.max(0, protection);
        markDirty();
    }

    public void setRank(int rank) {
        int clamped = Math.max(0, rank);

        getServerWorld().ifPresent(world ->
                getTeam(world).ifPresent(team ->
                        TeamSavedData.modifyRank(world, team, r -> r + (clamped - this.rank))));

        this.rank = clamped;
        markDirty();
    }

    public Optional<TextFormatting> getColor() {
        return Optional.ofNullable(this.color).filter(TextFormatting::isColor);
    }

    public Optional<ScorePlayerTeam> getTeam(@Nullable World world) {
        if (world == null || this.team == null) return Optional.empty();
        return this.getTeamName().map(name -> world.getScoreboard().getTeam(name)).filter(Objects::nonNull);
    }

    public Optional<String> getTeamName() {
        return Optional.ofNullable(this.team);
    }

    @Override
    public void remove() {
        super.remove();
        Optional.ofNullable(world).ifPresent(w -> PlayerData.clearFlag(getPos(), w));
        getServerWorld().ifPresent(world -> {
            ChunkSavedData.removeProtection(world, new ChunkPos(getPos()));
            getTeam(world).ifPresent(team ->
                    TeamSavedData.modifyRank(world, team, r -> r - this.getRank()));
        });
    }

    public void returnBanner(boolean notify) {
        if (notify) getTeam(world).ifPresent(t -> {
            assert world != null;
            world.getPlayers().stream()
                    .filter(p -> p.isOnScoreboardTeam(t))
                    .forEach(p -> p.sendStatusMessage(new TranslationTextComponent("message.divide.flag_returned"), true));
        });

        if (world != null) world.setBlockState(getPos(), world.getBlockState(getPos()).with(Flag.MISSING, false));
    }

    public boolean canBeBrokenBy(PlayerEntity player) {
        return !isProtected()
                && !getBlockState().get(Flag.MISSING)
                && !isInTeam(player)
                && !PlayerData.hasFlag(player);
    }

    public void stolenBy(PlayerEntity player) {
        PlayerData.clearFlag(player, false);
        setRank(rank - 1);
        setProtection(STOLEN_PROT);
    }

    public void brokenBy(PlayerEntity player) {
        World world = getWorld();

        getTeam(world).ifPresent(team -> {
            assert world != null;

            PlayerData.setFlag(player, getPos());

            player.sendStatusMessage(new TranslationTextComponent("message.divide.flag_broken", team.getDisplayName()), true);
            world.getPlayers().stream()
                    .filter(p -> p.isOnScoreboardTeam(team))
                    .forEach(p -> {
                        p.sendStatusMessage(new TranslationTextComponent("message.divide.flag_taken", team).applyTextStyle(TextFormatting.RED), true);
                        p.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.NEUTRAL, 1F, 1F);
                    });
        });
    }

    public boolean isInTeam(Entity entity) {
        return Optional.ofNullable(entity.getTeam()).map(Team::getName).filter(n -> n.equals(this.team)).isPresent();
        //return this.getTeam(entity.world).map(entity::isOnScoreboardTeam).orElse(false);
    }

    public void setTeam(String name) {
        if (world != null) setTeam(world.getScoreboard().getTeam(name));
        else {
            this.team = name;
            markDirty();
        }
    }

    public void setTeam(Team team) {
        if (world instanceof ServerWorld) getTeam(world).ifPresent(old -> {
            TeamSavedData.modifyRank((ServerWorld) world, old, r -> r - this.rank);
        });

        if (team != null) {
            getServerWorld().ifPresent(world -> {
                TeamSavedData.modifyRank(world, team, r -> r + this.rank);
                ChunkPos pos = new ChunkPos(getPos());
                ChunkSavedData.removeProtection(world, pos);
                ChunkSavedData.createProtection(world, pos, new ChunkProtection(ChunkProtection.Type.flag, team.getName()));
            });
            this.team = team.getName();
            this.color = team.getColor();
        } else {
            this.team = null;
            this.color = null;
        }
        markDirty();
    }

    public FlagTile(int rank) {
        this();
        setRank(rank);
    }

    public FlagTile() {
        super(Content.FLAG_TILE_TYPE.get());
    }

    @Override
    public void writePacketNBT(CompoundNBT cmp) {
        if (this.team != null) cmp.putString("team", this.team);
        if (this.color != null) cmp.putString("color", this.color.getFriendlyName());
        cmp.putInt("protection", this.protection);
        cmp.putInt("rank", this.rank);
    }

    @Override
    public void readPacketNBT(CompoundNBT cmp) {
        if (cmp.contains("color")) this.color = TextFormatting.getValueByName(cmp.getString("color"));
        if (cmp.contains("rank")) setRank(cmp.getInt("rank"));
        if (cmp.contains("team")) setTeam(cmp.getString("team"));
        if (cmp.contains("protection")) this.protection = cmp.getInt("protection");
    }
}
