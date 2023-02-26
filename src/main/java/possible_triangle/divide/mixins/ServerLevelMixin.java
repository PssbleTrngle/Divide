package possible_triangle.divide.mixins;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import possible_triangle.divide.GameData;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(at = @At("HEAD"), method = "tickTime()V", cancellable = true)
    private void noTickingIfPaused(CallbackInfo callback) {
        var self = (ServerLevel) (Object) (this);
        if (GameData.Companion.getDATA().get(self.getServer()).getPaused()) callback.cancel();
    }

}
