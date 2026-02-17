package net.dahicksfamily.sgt.dimension;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

public class SpaceDimensionEffects extends DimensionSpecialEffects {

    public SpaceDimensionEffects() {
        super(Float.NaN, true, SkyType.NONE, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float brightness) {
        return new Vec3(0, 0, 0);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }
}