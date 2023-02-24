package possible_triangle.divide.mixins;

import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import possible_triangle.divide.hacks.PacketIntercepting;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Inject(at = @At("HEAD"), method = "sendPacket(Lnet/minecraft/network/Packet;)V", cancellable = true)
    public void transformPacket(Packet<?> packet, CallbackInfo callback) {
        var self = (ServerPlayNetworkHandler) (Object) this;
        var transformed = PacketIntercepting.INSTANCE.transformPacket(packet, self.player);
        if(transformed != packet) {
            self.sendPacket(transformed, null);
            callback.cancel();
        }
    }

}
