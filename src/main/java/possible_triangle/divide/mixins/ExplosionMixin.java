package possible_triangle.divide.mixins;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import possible_triangle.divide.crates.CrateEvents;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow
    @Final
    private ObjectArrayList<BlockPos> affectedBlocks;

    @Shadow
    @Final
    private World world;

    @Inject(at = @At("HEAD"), method = "collectBlocksAndDamageEntities()V")
    public void collectBlocksAndDamageEntities(CallbackInfo ci) {
        var server = world.getServer();
        if (server != null) CrateEvents.INSTANCE.modifyExplosion(server, affectedBlocks);
    }

}
