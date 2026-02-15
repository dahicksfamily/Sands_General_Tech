package net.dahicksfamily.sgt.item;

import net.dahicksfamily.sgt.SGT;
import net.dahicksfamily.sgt.item.Functional.TelescopeItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SGT.MOD_ID);

    public static final RegistryObject<Item> TELESCOPE = ITEMS.register("telescope",
            () -> new TelescopeItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(0)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
