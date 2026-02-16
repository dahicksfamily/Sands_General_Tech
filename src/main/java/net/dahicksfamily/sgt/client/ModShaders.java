package net.dahicksfamily.sgt.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.dahicksfamily.sgt.SGT;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = SGT.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModShaders {

    private static ShaderInstance celestialBodyShader;

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        new ResourceLocation(SGT.MOD_ID, "celestial_body"),
                        DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL
                ),
                shader -> celestialBodyShader = shader
        );
    }

    public static ShaderInstance getCelestialBodyShader() {
        return celestialBodyShader;
    }

    // Helper methods to set uniforms
    public static void setLightDirection(Vector3f direction) {
        if (celestialBodyShader != null) {
            var uniform = celestialBodyShader.getUniform("LightDirection");
            if (uniform != null) {
                uniform.set(direction.x, direction.y, direction.z);
            }
        }
    }

    public static void setAmbientLight(float ambient) {
        if (celestialBodyShader != null) {
            var uniform = celestialBodyShader.getUniform("AmbientLight");
            if (uniform != null) {
                uniform.set(ambient);
            }
        }
    }
}