package net.dahicksfamily.sgt.mixin;

import net.dahicksfamily.sgt.time.GlobalTime;
import net.minecraft.world.level.LevelTimeAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LevelTimeAccess.class)
public interface LevelTimeAccessMixin {
    /**
     * @author SGT
     * @reason Override with GlobalTime for dynamic sun angle
     */
    @Overwrite
    default float getTimeOfDay(float partialTick) {
        return GlobalTime.getInstance().getSunAngle();
    }
}