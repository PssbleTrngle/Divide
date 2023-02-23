package possible_triangle.divide.mixins;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import possible_triangle.divide.events.Eras;
import possible_triangle.divide.missions.Mission;

@Mixin(PlayerEntity.class)
public class PlayerMixin {

    @Inject(at = @At("HEAD"), method = "shouldDamagePlayer(Lnet/minecraft/entity/player/PlayerEntity;)Z", cancellable = true)
    public void canHarmPlayer(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        var self = (PlayerEntity) (Object) this;
        var server = self.getServer();
        if (server != null) {
            var isPeace = Eras.INSTANCE.isPeace(server);
            if (isPeace) cir.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "jump()V")
    public void jumpFromGround(CallbackInfo callback) {
        var self = (PlayerEntity) (Object) this;
        Mission.Companion.getJUMP().fulfill(self);
    }

}
