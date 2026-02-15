package net.dahicksfamily.sgt.client;

import net.dahicksfamily.sgt.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "sgt", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TelescopeZoomHandler {

    private static final float TELESCOPE_ZOOM = 0.001f;
    private static final double SENSITIVITY_SCALE = 0.000005;

    private static Double originalSensitivity = null;

    @SubscribeEvent
    public static void onFovModifier(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();

        Minecraft mc = Minecraft.getInstance();

        if (player.isUsingItem()) {
            ItemStack useItem = player.getUseItem();

            if (useItem.is(ModItems.TELESCOPE.get())) {

                event.setNewFovModifier(TELESCOPE_ZOOM);

                if (originalSensitivity == null) {
                    originalSensitivity = mc.options.sensitivity().get();
                    mc.options.sensitivity().set(originalSensitivity * SENSITIVITY_SCALE);
                }

                return;
            }
        }

        if (originalSensitivity != null) {
            mc.options.sensitivity().set(originalSensitivity);
            originalSensitivity = null;
        }
    }
}
