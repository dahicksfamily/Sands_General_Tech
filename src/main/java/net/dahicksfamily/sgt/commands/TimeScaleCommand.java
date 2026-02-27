package net.dahicksfamily.sgt.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.dahicksfamily.sgt.network.ModPackets;
import net.dahicksfamily.sgt.network.TimeScaleSyncPacket;
import net.dahicksfamily.sgt.time.GlobalTime;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class TimeScaleCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("timescale")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("scale", DoubleArgumentType.doubleArg(0.0))
                                .executes(ctx -> {
                                    double scale = DoubleArgumentType.getDouble(ctx, "scale");

                                    GlobalTime.getInstance().setTimeScale(scale);

                                    ModPackets.sendToAllClients(new TimeScaleSyncPacket(scale));

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Time scale set to " + scale), true);
                                    return 1;
                                })
                        )
        );
    }
}