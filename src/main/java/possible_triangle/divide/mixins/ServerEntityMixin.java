package possible_triangle.divide.mixins;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import possible_triangle.divide.missions.Mission;

@Mixin(ServerPlayerEntity.class)
public class ServerEntityMixin {

    /*
    @Inject(at = @At("RETURN"), method = "sendDirtyEntityData()V")
    public void sendDirtyEntityData(CallbackInfo callback) {
        var self = (ServerPlayerEntity) (Object) this;
        if (self.entity.getEntityData().isDirty()) PacketIntercepting.INSTANCE.updateData(self.entity, self.level.getServer());
    }

    @Inject(at = @At("RETURN"), method = "sendPairingData(Ljava/util/function/Consumer;)V")
    public void sendChanges(CallbackInfo callback) {
        var self = (ServerPlayerEntity) (Object) this;
        PacketIntercepting.INSTANCE.updateData(self.entity, self.level.getServer());
    }
    */

    @Inject(at = @At("RETURN"), method = "worldChanged(Lnet/minecraft/server/world/ServerWorld;)V")
    public void sendChanges(CallbackInfo callback) {
        var self = (ServerPlayerEntity) (Object) this;
        Mission.Companion.getDIMENSIONAL_TRAVEL().fulfill(self);
    }

}
