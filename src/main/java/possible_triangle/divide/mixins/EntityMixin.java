package possible_triangle.divide.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import possible_triangle.divide.missions.Mission;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(at = @At("HEAD"), method = "setSneaking(Z)V")
    public void setSneaking(boolean sneaking, CallbackInfo ci) {
        var self = (Entity) (Object) this;
        if(sneaking && self instanceof PlayerEntity player) {
            Mission.Companion.getSNEAK().fulfill(player);
        }
    }

}
