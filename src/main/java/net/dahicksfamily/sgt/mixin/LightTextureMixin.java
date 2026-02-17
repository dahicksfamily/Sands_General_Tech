package net.dahicksfamily.sgt.mixin;

import net.dahicksfamily.sgt.client.LightmapAccess;
import net.dahicksfamily.sgt.client.TextureAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public class LightTextureMixin implements LightmapAccess {

    @Shadow
    private DynamicTexture lightTexture;

    @Shadow
    private float blockLightRedFlicker;

    @Shadow
    private boolean updateLightTexture;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void afterInit(GameRenderer renderer, Minecraft minecraft, CallbackInfo ci) {
        ((TextureAccess) this.lightTexture).sgt_enableUploadHook();
    }

    @Override
    public float sgt_prevFlicker() {
        return this.blockLightRedFlicker;
    }

    @Override
    public boolean sgt_isDirty() {
        return this.updateLightTexture;
    }
}