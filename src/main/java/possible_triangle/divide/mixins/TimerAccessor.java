package possible_triangle.divide.mixins;

import com.google.common.collect.Table;
import net.minecraft.world.level.timers.TimerQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TimerQueue.class)
public interface TimerAccessor<T> {

    @Accessor
    Table<String, Long, TimerQueue.Event<T>> getEvents();

}
