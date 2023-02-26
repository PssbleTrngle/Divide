package possible_triangle.divide.mixins;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import possible_triangle.divide.crates.CrateScheduler;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(at = @At("HEAD"), method = "onDestroyed")
    public void onDestroyed(ItemEntity entity, CallbackInfo callback) {
        var self = (ItemStack) (Object) this;
        var server = entity.getServer();
        if (server != null) CrateScheduler.INSTANCE.saveItem(server, self);
    }

}
