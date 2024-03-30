package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import com.mojang.brigadier.StringReader;
import meteordevelopment.meteorclient.commands.arguments.CompoundNbtTagArgumentType;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;

import java.util.*;
import java.util.List;

public class CustomHead extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgExtra = settings.createGroup("Extra");

    private final Setting<java.util.List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .description("Select items to be shown on head.")
        .defaultValue(List.of())
        .build()
    );
    private final Setting<Integer> headDelay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay.")
        .defaultValue(2)
        .min(1)
        .sliderMax(20)
        .build()
    );
    private final Setting<Boolean> isCustomNbt = sgGeneral.add(new BoolSetting.Builder()
        .name("enable-custom-nbt")
        .description("Enable custom NBT.")
        .defaultValue(false)
        .build()
    );
    private final Setting<List<String>> customNbt = sgGeneral.add(new StringListSetting.Builder()
        .name("custom-nbt")
        .description("Custom NBT to set to the items.")
        .defaultValue(List.of("{Enchantments:[{id:\"minecraft:aqua_affinity\",lvl:0s}]}"))
        .visible(isCustomNbt::get)
        .build()
    );
    private final Setting<Boolean> toggleOnLog = sgExtra.add(new BoolSetting.Builder()
        .name("toggle-on-log")
        .description("Disables RGB armor when you disconnect from a server.")
        .defaultValue(true)
        .build()
    );
    public CustomHead() {
        super(Addon.CATEGORY, "custom-head", "Sets custom item in head slot.");
    }

    private int ticks;
    boolean isIndex = false;
    boolean isHSB = false;
    private int nbtI;

    @Override
    public void onActivate() {
        if (!mc.player.getAbilities().creativeMode) {
            error("Creative mode only.");
            this.toggle();
        }
        nbtI = 0;
    }
    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (!toggleOnLog.get()) return;
        toggle();
    }
    @EventHandler
    private void onTick(TickEvent.Post event) {
        ticks++;
        if (ticks % headDelay.get() == 0) {
            List<Item> selectedItems = new ArrayList<>(items.get());
            Collections.shuffle(selectedItems, new Random());
            if (selectedItems.isEmpty()) return;
            ItemStack itemStack = new ItemStack(selectedItems.get(0 % selectedItems.size()));

            // Apply enchantments
            if (!customNbt.get().isEmpty() && isCustomNbt.get()) {
                if (nbtI >= customNbt.get().size()) nbtI = 0;
                String nbt = customNbt.get().get(nbtI);
                try {
                    if(!Objects.equals(nbt, "")) {
                        itemStack.setNbt(CompoundNbtTagArgumentType.create().parse(new StringReader(nbt)));
                    }
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
                nbtI++;
            }
            CreativeInventoryActionC2SPacket packet = new CreativeInventoryActionC2SPacket(5, itemStack);
            mc.player.networkHandler.sendPacket(packet);
        }
    }
}
