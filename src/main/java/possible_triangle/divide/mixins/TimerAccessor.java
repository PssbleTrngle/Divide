package possible_triangle.divide.mixins;

import com.google.common.collect.Table;
import net.minecraft.world.timer.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Timer.class)
public interface TimerAccessor<T> {

    @Accessor
    Table<String, Long, Timer.Event<T>> getEventsByName();

}
