package net.dahicksfamily.sgt.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.dahicksfamily.sgt.time.GlobalTime;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class TimeScaleCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("timescale")
                .then(Commands.argument("scale", DoubleArgumentType.doubleArg(0.0, 100000000.0))
                        .executes(ctx -> {
                            double scale = DoubleArgumentType.getDouble(ctx, "scale");
                            GlobalTime.getInstance().setTimeScale(scale);
                            ctx.getSource().sendSuccess(() ->
                                    Component.literal("Time scale set to " + scale + "x"), true);
                            return 1;
                        })
                )
                .executes(ctx -> {
                    double scale = GlobalTime.getInstance().getTimeScale();
                    ctx.getSource().sendSuccess(() ->
                            Component.literal("Current time scale: " + scale + "x"), false);
                    return 1;
                })
        );
    }
}