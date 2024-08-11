package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.EnchantmentListSetting;

import java.util.*;

public class ItemDropper extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> dropHeld = sgGeneral.add(new BoolSetting.Builder()
        .name("drop-held")
        .description("Only drops held item.")
        .defaultValue(false)
        .build()
    );
    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .description("Select items to be dropped.")
        .visible(() -> !dropHeld.get())
        .build()
    );
    private final Setting<Set<RegistryKey<Enchantment>>> enchants = sgGeneral.add(new EnchantmentListSetting.Builder()
        .name("Enchants")
        .description("Enchantments to apply to the dropped items.")
        .visible(() -> !dropHeld.get())
        .build());
    private final Setting<Integer> enchantmentLevel = sgGeneral.add(new IntSetting.Builder()
        .name("enchantment-level")
        .description("Set enchantments for the items.")
        .defaultValue(1)
        .min(1)
        .sliderMax(10)
        .visible(() -> !dropHeld.get())
        .build()
    );
    private final Setting<Boolean> isCustomName = sgGeneral.add(new BoolSetting.Builder()
        .name("custom-name")
        .description("Enable custom name for items.")
        .defaultValue(false)
        .build()
    );
    private final Setting<String> customName = sgGeneral.add(new StringSetting.Builder()
        .name("name")
        .description("Set custom name for the items")
        .defaultValue("&fCUSTOM &cNAME")
        .visible(isCustomName::get)
        .build()
    );
    private final Setting<Integer> speed = sgGeneral.add(new IntSetting.Builder()
        .name("speed")
        .description("WARNING: High speeds will cause a ton of lag and can easily crash the game!")
        .defaultValue(1)
        .min(1)
        .sliderMax(30)
        .build()
    );
    private final Setting<Integer> stackSize = sgGeneral.add(new IntSetting.Builder()
        .name("stack-size")
        .description("How many items to place in each stack. Doesn't seem to affect performance.")
        .defaultValue(1)
        .min(1)
        .max(64)
        .sliderMax(64)
        .build()
    );
    public ItemDropper() {
        super(Addon.CATEGORY, "item-dropper", "Generates selected items and drops them from your inventory.");
    }
    @Override
    public void onActivate() {
        if (!mc.player.getAbilities().creativeMode) {
            error("Creative mode only.");
            this.toggle();
        }
    }
    @EventHandler
    private void onTick(TickEvent.Post event) {
        List<Item> selectedItems = new ArrayList<> (items.get());
        if (!mc.player.getAbilities().creativeMode) {
            this.toggle();
            error("Creative mode only.");
        }
        Collections.shuffle(selectedItems, new Random());
        for (int i = 9; i < 9 + speed.get(); i++) {
            Item itemToGenerate = selectedItems.isEmpty() ? Items.AIR : selectedItems.get(i % selectedItems.size());
            ItemStack heldItem = mc.player.getInventory().getMainHandStack();
            ItemStack itemStack = new ItemStack(dropHeld.get() ? heldItem.getItem() : itemToGenerate, stackSize.get());

            if (dropHeld.get()) {
                itemStack.applyComponentsFrom(heldItem.getComponents());
            }

            Registry<Enchantment> enchantmentRegistry = mc.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);

            for (RegistryKey<Enchantment> enchantKey : enchants.get()) {
                RegistryEntry<Enchantment> enchantEntry = enchantmentRegistry.entryOf(enchantKey);
                itemStack.addEnchantment(enchantEntry, enchantmentLevel.get());
            }

            if (!customName.get().isEmpty() && isCustomName.get()) {
                itemStack.remove(DataComponentTypes.CUSTOM_DATA);
                itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(customName.get().replaceAll("&", "§")));
            }

            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(i, itemStack));
        }
        for (int i = 9; i < 9 + speed.get(); i++) {
            InvUtils.drop().slot(i);
        }
    }
}
