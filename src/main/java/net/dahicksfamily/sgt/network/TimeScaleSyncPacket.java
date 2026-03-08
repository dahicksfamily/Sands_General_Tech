package net.dahicksfamily.sgt.network;

import net.dahicksfamily.sgt.time.GlobalTime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

 
public class TimeScaleSyncPacket {

    private final double timeScale;

    public TimeScaleSyncPacket(double timeScale) {
        this.timeScale = timeScale;
    }

    public static void encode(TimeScaleSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeDouble(pkt.timeScale);
    }

    public static TimeScaleSyncPacket decode(FriendlyByteBuf buf) {
        return new TimeScaleSyncPacket(buf.readDouble());
    }

     
    public static void handle(TimeScaleSyncPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> GlobalTime.getInstance().setTimeScale(pkt.timeScale));
        ctx.get().setPacketHandled(true);
    }
}