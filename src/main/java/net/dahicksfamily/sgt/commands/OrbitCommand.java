package net.dahicksfamily.sgt.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.dahicksfamily.sgt.network.ModPackets;
import net.dahicksfamily.sgt.network.OrbitPacket;
import net.dahicksfamily.sgt.space.CelestialBody;
import net.dahicksfamily.sgt.space.PlanetsProvider;
import net.dahicksfamily.sgt.SkyRendering.SpaceObjectRenderer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class OrbitCommand {

 
    private static final SuggestionProvider<CommandSourceStack> BODY_SUGGESTIONS =
            (ctx, builder) -> {
                PlanetsProvider.getAllBodies().forEach(b -> builder.suggest(b.name));
                return builder.buildFuture();
            };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("orbit")
                        .requires(src -> src.hasPermission(2)) 
                        .then(Commands.argument("body", StringArgumentType.word())
                                .suggests(BODY_SUGGESTIONS)
                                .then(Commands.argument("semiMajorAxis", DoubleArgumentType.doubleArg(0))
                                        .then(Commands.argument("eccentricity", DoubleArgumentType.doubleArg(0, 0.9999))
                                                .then(Commands.argument("inclination", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> {
                                                            String bodyName  = StringArgumentType.getString(ctx, "body");
                                                            double sma       = DoubleArgumentType.getDouble(ctx, "semiMajorAxis");
                                                            double ecc       = DoubleArgumentType.getDouble(ctx, "eccentricity");
                                                            double incDeg    = DoubleArgumentType.getDouble(ctx, "inclination");

 
                                                            CelestialBody parent = PlanetsProvider.getBodyByName(bodyName);
                                                            if (parent == null) {
                                                                ctx.getSource().sendFailure(
                                                                        Component.literal("Unknown body: " + bodyName));
                                                                return 0;
                                                            }
                                                            CelestialBody orbiter = PlanetsProvider.getBodyByName("Space Orbiter");
                                                            if (orbiter == null) {
                                                                ctx.getSource().sendFailure(
                                                                        Component.literal("Space Orbiter not found."));
                                                                return 0;
                                                            }

 
                                                            double incRad = Math.toRadians(incDeg);
                                                            double period; 
                                                            if (parent.mass > 0) {
                                                                double G     = 6.674e-11 * (1.0 / 1e9) * (86400.0 * 86400.0);
                                                                double smaKm = (parent instanceof net.dahicksfamily.sgt.space.Star)
                                                                        ? sma * 1.496e8 : sma;
                                                                period = 2 * Math.PI * Math.sqrt(Math.pow(smaKm, 3) / (G * parent.mass));
                                                            } else {
                                                                period = orbiter.period;
                                                            }

 
                                                            ModPackets.sendToAllClients(new OrbitPacket(bodyName, sma, ecc, incRad, period));

                                                            ctx.getSource().sendSuccess(
                                                                    () -> Component.literal(
                                                                            "Space Orbiter → parent=" + parent.name
                                                                                    + " sma=" + sma
                                                                                    + " ecc=" + ecc
                                                                                    + " inc=" + incDeg + "°"
                                                                                    + " period=" + String.format("%.4f", period) + "d"),
                                                                    true);
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
        );
    }

     
    private static double toKm(double sma, CelestialBody parent) {
 
 
 
        if (parent instanceof net.dahicksfamily.sgt.space.Star) {
            return sma * 1.496e8; 
        }
        return sma; 
    }
}