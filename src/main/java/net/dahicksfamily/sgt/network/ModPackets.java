package net.dahicksfamily.sgt.network;

import net.dahicksfamily.sgt.SGT;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModPackets {

    private static final String VERSION = "1";
    private static int nextId = 0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SGT.MOD_ID, "main"),
            () -> VERSION,
            VERSION::equals,
            VERSION::equals
    );

    public static void register() {
        CHANNEL.messageBuilder(TimeScaleSyncPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TimeScaleSyncPacket::encode)
                .decoder(TimeScaleSyncPacket::decode)
                .consumerMainThread(TimeScaleSyncPacket::handle)
                .add();
    }

    public static <T> void sendToAllClients(T packet) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }
}