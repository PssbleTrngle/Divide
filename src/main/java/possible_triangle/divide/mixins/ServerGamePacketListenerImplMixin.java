package possible_triangle.divide.mixins;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import possible_triangle.divide.logic.Glowing;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Inject(at = @At("HEAD"), method = "send(Lnet/minecraft/network/protocol/Packet;)V", cancellable = true)
    public void transformPacket(Packet<?> packet, CallbackInfo callback) {
        var self = (ServerGamePacketListenerImpl) (Object) this;
        var transformed = Glowing.INSTANCE.transformPacket(packet, self.player);
        if(transformed != packet) {
            self.send(transformed, null);
            callback.cancel();
        }
    }

}
