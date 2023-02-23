package possible_triangle.divide.mixins;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import possible_triangle.divide.missions.Mission;

@Mixin(CraftingResultSlot.class)
public class ResultSlotMixin {

    @Shadow @Final private PlayerEntity player;

    @Inject(at = @At("HEAD"), method = "onCrafted(Lnet/minecraft/item/ItemStack;)V")
    public void onCrafted(ItemStack stack, CallbackInfo ci) {
        Mission.Companion.onCrafted(player, stack);
    }

}
