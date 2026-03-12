package net.dahicksfamily.sgt.background;

import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SupernovaManager {

    private static final List<SupernovaEvent> active = new ArrayList<>();
    private static final long CHECK_INTERVAL_TICKS = 1200;
    private static long lastCheckTick = -1;

    private static final double CHANCE_PER_CHECK = 0.04f;


    private static final long  FLASH_TICKS = 80;
    private static final float FLASH_END   = 0.12f;

    public static List<SupernovaEvent> getActive() { return active; }

    public static float getProgress(SupernovaEvent sn, long gameTick) {
        long elapsed = gameTick - sn.startTick;

        if (elapsed <= 0) return 0.0f;
        if (elapsed >= sn.durationTicks) return 1.0f;

        if (elapsed < FLASH_TICKS) {
            float flashFrac = (float) elapsed / FLASH_TICKS;
            return flashFrac * FLASH_END;
        } else {
            long nebulaTicks   = sn.durationTicks - FLASH_TICKS;
            long nebulaElapsed = elapsed - FLASH_TICKS;
            float nebulaFrac   = (float) nebulaElapsed / nebulaTicks;
            return FLASH_END + nebulaFrac * (1.0f - FLASH_END);
        }
    }

    public static float getCurrentSize(SupernovaEvent sn, long gameTick) {
        float progress = getProgress(sn, gameTick);

        if (progress <= FLASH_END) {
            float flashT = progress / FLASH_END;
            float bloomFrac = flashT * flashT;
            return mix(0.002f, sn.peakSize * 0.18f, bloomFrac);
        } else {
            float nebT = (progress - FLASH_END) / (1.0f - FLASH_END);
            float growFrac = (float) Math.sqrt(nebT);
            return mix(sn.peakSize * 0.18f, sn.peakSize, growFrac);
        }
    }

    private static float mix(float a, float b, float t) {
        return a + (b - a) * Math.max(0f, Math.min(1f, t));
    }

    public static void tick(long gameTick, long worldSeed) {
        Iterator<SupernovaEvent> it = active.iterator();
        while (it.hasNext()) if (it.next().isDone(gameTick)) it.remove();

        if (gameTick - lastCheckTick < CHECK_INTERVAL_TICKS) return;
        lastCheckTick = gameTick;

        Random rng = new Random(worldSeed ^ gameTick);
        if (rng.nextDouble() < CHANCE_PER_CHECK) {
            spawnSupernova(rng, gameTick, worldSeed);
        }
    }

    private static void spawnSupernova(Random rng, long gameTick, long seed) {
        SupernovaEvent sn = new SupernovaEvent();
        sn.seed = rng.nextFloat();

        float lon = rng.nextFloat() * (float)(Math.PI * 2);
        float lat = (float)(rng.nextGaussian() * 0.30f);
        float cl  = (float)Math.cos(lat);
        sn.x = cl * (float)Math.cos(lon);
        sn.y = (float)Math.sin(lat);
        sn.z = cl * (float)Math.sin(lon);

        sn.r = 1.0f; sn.g = 0.95f; sn.b = 0.90f;
        sn.startTick     = gameTick;
        sn.durationTicks = 48000 + rng.nextInt(48000);
        sn.peakSize      = 0.1f + rng.nextFloat() * 0.5f;

        active.add(sn);

        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§e✦ A supernova has appeared in the sky!"),
                    true);
        }
    }

    public static void cleanup() { active.clear(); lastCheckTick = -1; }
}