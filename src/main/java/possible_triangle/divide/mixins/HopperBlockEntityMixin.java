package possible_triangle.divide.mixins;

import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import possible_triangle.divide.crates.CrateEvents;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {

    @Inject(at = @At("HEAD"), method = "extract(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/Hopper;)Z", cancellable = true)
    private static void preventStuff(World world, Hopper hopper, CallbackInfoReturnable<Boolean> callback) {
        if(CrateEvents.INSTANCE.preventsSucking(world, hopper)) callback.setReturnValue(false);
    }

}
