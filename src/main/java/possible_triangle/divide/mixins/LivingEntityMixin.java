package possible_triangle.divide.mixins;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import possible_triangle.divide.logic.DeathEvents;
import possible_triangle.divide.missions.Mission;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(at = @At("HEAD"), method = "eat")
    public void eat(Level level, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        var self = (LivingEntity) (Object) this;
        Mission.Companion.onEat(self, stack);
    }

    @Inject(at = @At("HEAD"), method = "dropAllDeathLoot", cancellable = true)
    public void drop(DamageSource source, CallbackInfo ci) {
        var self = (LivingEntity) (Object) this;
        if (DeathEvents.INSTANCE.modifyPlayerDrops(self, source)) {
            ci.cancel();
        }
    }

}
