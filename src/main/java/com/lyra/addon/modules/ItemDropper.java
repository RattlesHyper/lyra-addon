package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.EnchantmentListSetting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ItemDropper extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .description("Select items to be dropped.")
        .defaultValue(List.of())
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
    private final Setting<String> customName = sgGeneral.add(new StringSetting.Builder()
        .name("custom-name")
        .description("Custom name for the dropped items")
        .defaultValue("")
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
    private final Setting<List<Enchantment>> enchants = sgGeneral.add(new EnchantmentListSetting.Builder()
        .name("enchants")
        .description("Enchantments to apply to the dropped items.")
        .defaultValue(List.of())
        .build()
    );
    private final Setting<Integer> enchantmentLevel = sgGeneral.add(new IntSetting.Builder()
        .name("enchantment-level")
        .description("WARNING: Enchantments that are higher than level 5 or 4 may not apply on servers.")
        .defaultValue(1)
        .min(1)
        .sliderMax(10)
        .build()
    );
    private final Setting<Boolean> dropHeld = sgGeneral.add(new BoolSetting.Builder()
        .name("drop-held")
        .description("Only drops held item.")
        .defaultValue(false)
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
            ItemStack itemStack = new ItemStack(dropHeld.get() ? mc.player.getMainHandStack().getItem() : itemToGenerate, stackSize.get());
            if(dropHeld.get()) {
                itemStack.setNbt(mc.player.getMainHandStack().getNbt());
            }
            for (Enchantment enchantment: enchants.get()) {
                itemStack.addEnchantment(enchantment, enchantmentLevel.get());
            }
            if (!customName.get().isEmpty()) {
                itemStack.setCustomName(Text.of(customName.get().replaceAll("&", "§")));
            }
            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(i, itemStack));
        }
        for (int i = 9; i < 9 + speed.get(); i++) {
            InvUtils.drop().slot(i);
        }
    }
}
