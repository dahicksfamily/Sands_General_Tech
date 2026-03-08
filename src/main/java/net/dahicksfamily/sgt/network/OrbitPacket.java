package net.dahicksfamily.sgt.network;

import net.dahicksfamily.sgt.SkyRendering.SpaceObjectRenderer;
import net.dahicksfamily.sgt.space.CelestialBody;
import net.dahicksfamily.sgt.space.PlanetsProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OrbitPacket {

    private final String parentName;
    private final double semiMajorAxis;
    private final double eccentricity;
    private final double inclinationRad;
    private final double period;

    public OrbitPacket(String parentName, double semiMajorAxis,
                       double eccentricity, double inclinationRad, double period) {
        this.parentName    = parentName;
        this.semiMajorAxis = semiMajorAxis;
        this.eccentricity  = eccentricity;
        this.inclinationRad = inclinationRad;
        this.period        = period;
    }

 
    public static void encode(OrbitPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.parentName);
        buf.writeDouble(pkt.semiMajorAxis);
        buf.writeDouble(pkt.eccentricity);
        buf.writeDouble(pkt.inclinationRad);
        buf.writeDouble(pkt.period);
    }

 
    public static OrbitPacket decode(FriendlyByteBuf buf) {
        return new OrbitPacket(
                buf.readUtf(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
        );
    }

 
    public static void handle(OrbitPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> applyOnClient(pkt))
        );
        ctx.setPacketHandled(true);
    }

    private static void applyOnClient(OrbitPacket pkt) {
        CelestialBody orbiter = PlanetsProvider.getBodyByName("Space Orbiter");
        CelestialBody parent  = PlanetsProvider.getBodyByName(pkt.parentName);
        if (orbiter == null || parent == null) return;

        orbiter.parent        = parent;
        orbiter.semiMajorAxis = pkt.semiMajorAxis;
        orbiter.eccentricity  = pkt.eccentricity;
        orbiter.inclination   = pkt.inclinationRad;
        orbiter.period        = pkt.period;

        SpaceObjectRenderer.invalidatePositionCache();
    }
}