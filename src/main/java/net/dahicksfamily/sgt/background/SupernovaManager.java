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

 
    private static final double CHANCE_PER_CHECK = 0.04;

    public static List<SupernovaEvent> getActive() { return active; }

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

 
        float lon = rng.nextFloat() * (float)(Math.PI*2);
        float lat = (float)(rng.nextGaussian() * 0.30f);
        float cl  = (float)Math.cos(lat);
        sn.x = cl*(float)Math.cos(lon);
        sn.y = (float)Math.sin(lat);
        sn.z = cl*(float)Math.sin(lon);

 
        sn.r = 1.0f; sn.g = 0.95f; sn.b = 0.90f;
        sn.startTick     = gameTick;
        sn.durationTicks = 48000 + rng.nextInt(48000); 
        sn.peakSize      = 4f + rng.nextFloat()*8f;

        active.add(sn);

 
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§e✦ A supernova has appeared in the sky!"),
                    true);
        }
    }

    public static void clear() { active.clear(); lastCheckTick = -1; }
}