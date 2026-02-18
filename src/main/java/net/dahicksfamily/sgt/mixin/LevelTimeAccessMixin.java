package net.dahicksfamily.sgt.mixin;

import net.dahicksfamily.sgt.time.GlobalTime;
import net.minecraft.world.level.LevelTimeAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LevelTimeAccess.class)
public interface LevelTimeAccessMixin {

    /**
     * @author SGT
     */
    @Unique
    default long sand_sGeneralTech$getGameTime() {
        return GlobalTime.getInstance().getMinecraftTicks();
    }

    /**
     * @author SGT
     */
    @Unique
    default long sand_sGeneralTech$getDayTime() {
        long ticks = GlobalTime.getInstance().getMinecraftTicks();
        return ticks % 24000L;
    }
}