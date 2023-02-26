package possible_triangle.divide.mixins;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import possible_triangle.divide.events.Eras;
import possible_triangle.divide.missions.Mission;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(at = @At("HEAD"), method = "canHarmPlayer(Lnet/minecraft/world/entity/player/Player;)Z", cancellable = true)
    public void canHarmPlayer(Player player, CallbackInfoReturnable<Boolean> cir) {
        var self = (Player) (Object) this;
        var server = self.getServer();
        if (server != null) {
            var isPeace = Eras.INSTANCE.isPeace(server);
            if (isPeace) cir.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "jumpFromGround()V")
    public void jumpFromGround(CallbackInfo callback) {
        var self = (Player) (Object) this;
        Mission.Companion.getJUMP().fulfill(self);
    }

    @Inject(at = @At("HEAD"), method = "updatePlayerPose()V")
    public void updatePlayerPose(CallbackInfo ci) {
        var self = (Player) (Object) this;
        if(self.getPose() == Pose.CROUCHING) {
            Mission.Companion.getSNEAK().fulfill(self);
        }
    }

}
