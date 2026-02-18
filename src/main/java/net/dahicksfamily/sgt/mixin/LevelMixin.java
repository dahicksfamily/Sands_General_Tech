package net.dahicksfamily.sgt.mixin;

import net.dahicksfamily.sgt.time.GlobalTime;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class LevelMixin {
    @Inject(method = "getGameTime", at = @At("HEAD"), cancellable = true)
    private void overrideGameTime(CallbackInfoReturnable<Long> cir) {
        cir.setReturnValue(GlobalTime.getInstance().getMinecraftTicks());
    }

    @Inject(method = "getDayTime", at = @At("HEAD"), cancellable = true)
    private void overrideDayTime(CallbackInfoReturnable<Long> cir) {
        long ticks = GlobalTime.getInstance().getMinecraftTicks();
        cir.setReturnValue(ticks % 24000L);
    }
}