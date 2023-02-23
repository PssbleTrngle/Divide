package possible_triangle.divide.mixins;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import possible_triangle.divide.missions.Mission;
import possible_triangle.divide.reward.Action;
import possible_triangle.divide.reward.Reward;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract void calculateEntityAnimation(LivingEntity p_21044_, boolean p_21045_);

    @Inject(at = @At("HEAD"), method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;")
    public void eat(Level level, ItemStack stack, CallbackInfoReturnable<ItemStack> callback) {
        var self = (LivingEntity) (Object) this;
        Mission.Companion.onEat(self, stack);
    }

    @Inject(at = @At("HEAD"), method = "canBeSeenAsEnemy()Z", cancellable = true)
    public void canBeSeenAsEnemy(CallbackInfoReturnable<Boolean> callback) {
        var self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer player) {
            if (Action.Companion.isRunning(player.server, Reward.Companion.getHIDE_FROM_MONSTERS(), true, ctx -> ctx.targetPlayers().contains(player))) {
                callback.setReturnValue(false);
            }
        }
    }

}
