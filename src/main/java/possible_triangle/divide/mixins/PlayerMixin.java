package possible_triangle.divide.mixins;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import possible_triangle.divide.events.Eras;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(at = @At("HEAD"), method = "canHarmPlayer(Lnet/minecraft/world/entity/player/Player;)Z", cancellable = true)
    public void canHarmPlayer(Player player, CallbackInfoReturnable<Boolean> callback) {
        var self = (Player) (Object) this;
        var server = self.getServer();
        if (server != null) {
            var isPeace = Eras.Data.Companion.get(server);
            if (isPeace) callback.setReturnValue(false);
        }
    }

}
