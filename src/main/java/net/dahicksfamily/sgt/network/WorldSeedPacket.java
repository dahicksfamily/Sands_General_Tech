package net.dahicksfamily.sgt.network;

import net.dahicksfamily.sgt.background.StarFieldRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class WorldSeedPacket {
    private final long seed;
    public WorldSeedPacket(long seed) { this.seed = seed; }

    public static void encode(WorldSeedPacket p, FriendlyByteBuf b) { b.writeLong(p.seed); }
    public static WorldSeedPacket decode(FriendlyByteBuf b) { return new WorldSeedPacket(b.readLong()); }

    public static void handle(WorldSeedPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        StarFieldRenderer.initialize(pkt.seed)));
        ctx.get().setPacketHandled(true);
    }
}