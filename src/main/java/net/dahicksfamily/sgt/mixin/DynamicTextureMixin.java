package net.dahicksfamily.sgt.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.dahicksfamily.sgt.client.TextureAccess;
import net.dahicksfamily.sgt.client.TrueDarkness;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DynamicTexture.class)
public class DynamicTextureMixin implements TextureAccess {

    @Shadow
    private NativeImage pixels;

    private boolean enableHook = false;

    @Inject(method = "upload", at = @At("HEAD"))
    private void onUpload(CallbackInfo ci) {
        if (this.enableHook && TrueDarkness.enabled && this.pixels != null) {
            for (int blockIndex = 0; blockIndex < 16; blockIndex++) {
                for (int skyIndex = 0; skyIndex < 16; skyIndex++) {
                    int color = this.pixels.getPixelRGBA(blockIndex, skyIndex);
                    int darkened = TrueDarkness.darken(color, blockIndex, skyIndex);
                    this.pixels.setPixelRGBA(blockIndex, skyIndex, darkened);
                }
            }
        }
    }

    @Override
    public void sgt_enableUploadHook() {
        this.enableHook = true;
    }
}