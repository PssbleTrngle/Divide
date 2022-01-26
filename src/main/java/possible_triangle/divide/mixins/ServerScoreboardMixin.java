package possible_triangle.divide.mixins;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Score;
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

    @Inject(at = @At("HEAD"), method = "onScoreChanged(Lnet/minecraft/world/scores/Score;)V")
    private void onScoreChanged(Score score, CallbackInfo callback) {
        Scores.INSTANCE.scoreUpdate(score, server);
    }

}
