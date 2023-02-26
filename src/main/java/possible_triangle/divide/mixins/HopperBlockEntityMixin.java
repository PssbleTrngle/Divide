package possible_triangle.divide.mixins;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import possible_triangle.divide.crates.CrateEvents;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {

    @Inject(at = @At("HEAD"), method = "suckInItems(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/entity/Hopper;)Z", cancellable = true)
    private static void preventStuff(Level level, Hopper hopper, CallbackInfoReturnable<Boolean> cir) {
        if(CrateEvents.INSTANCE.preventsSucking(level, hopper)) cir.setReturnValue(false);
    }

}
