package possible_triangle.divide.mixins;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import possible_triangle.divide.logic.DeathEvents;
import possible_triangle.divide.missions.Mission;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(at = @At("HEAD"), method = "eatFood(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;")
    public void eat(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        var self = (LivingEntity) (Object) this;
        Mission.Companion.onEat(self, stack);
    }

    @Inject(at = @At("HEAD"), method = "drop(Lnet/minecraft/entity/damage/DamageSource;)V", cancellable = true)
    public void drop(DamageSource source, CallbackInfo ci) {
        var self = (LivingEntity) (Object) this;
        if (DeathEvents.INSTANCE.modifyPlayerDrops(self, source)) {
            ci.cancel();
        }
    }

}
