package possible_triangle.divide.mixins;

import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import possible_triangle.divide.info.Scores;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(at = @At("HEAD"), method = "updateScore(Lnet/minecraft/scoreboard/ScoreboardPlayerScore;)V")
    private void onScoreChanged(ScoreboardPlayerScore score, CallbackInfo ci) {
        Scores.INSTANCE.scoreUpdate(score, server);
    }

}
