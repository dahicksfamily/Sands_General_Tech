package net.dahicksfamily.sgt;

import net.dahicksfamily.sgt.SkyRendering.CelestialBuffers;
import net.dahicksfamily.sgt.SkyRendering.SpaceObjectRenderer;
import net.dahicksfamily.sgt.block.ModBlocks;
import net.dahicksfamily.sgt.item.ModItems;
import net.dahicksfamily.sgt.keybind.ModKeyBindings;
import net.dahicksfamily.sgt.space.CelestialBody;
import net.dahicksfamily.sgt.space.PlanetsProvider;
import net.dahicksfamily.sgt.space.SolarSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SGT.MOD_ID)
public class SGT
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "sgt";

    public SGT(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            System.out.println("=== INITIALIZING SOLAR SYSTEM ===");

            PlanetsProvider.registerCelestialBodies();
            System.out.println("Registered bodies: " + PlanetsProvider.getAllBodies().size());

            SolarSystem.getInstance().initialize();
            System.out.println("Solar system initialized");

            // Print out registered bodies
            for (CelestialBody body : PlanetsProvider.getAllBodies()) {
                System.out.println("Body: " + body.name + " at semi-major axis: " + body.semiMajorAxis);
            }
        });
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
        }

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                var server = ServerLifecycleHooks.getCurrentServer();
                if (server != null) {
                    ServerLevel overworld = server.getLevel(Level.OVERWORLD);
                    if (overworld != null) {
                        SolarSystem.getInstance().tick();

                        if (ModKeyBindings.TOGGLE_PLANET_LABELS.consumeClick()) {
                            SpaceObjectRenderer.toggleLabels();
                        }
                    }
                }
            }
        }


        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null) {
                    SolarSystem.getInstance().syncWithWorld(mc.level);
                }
            }
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onRegisterEffects(RegisterDimensionSpecialEffectsEvent event) {
        }

        @SubscribeEvent
        public static void onClientSetup(final FMLClientSetupEvent event) {
            event.enqueueWork(CelestialBuffers::initialize);
        }

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            ModKeyBindings.register(event);
        }
    }
}
