package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import com.lyra.addon.utils.SetItem;
import meteordevelopment.meteorclient.events.world.PlaySoundEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

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

    private final Setting<Boolean> toggleOnLog = sgExtra.add(new BoolSetting.Builder()
        .name("toggle-on-log")
        .description("Disables when you disconnect from a server.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> blockSound = sgExtra.add(new BoolSetting.Builder()
        .name("block-sound")
        .description("Blocks armor equip sound.")
        .defaultValue(true)
        .build()
    );
    public CustomHead() {
        super(Addon.CATEGORY, "custom-head", "Sets custom item in head slot.");
    }

    private int ticks;

    @Override
    public void onActivate() {
        if (!mc.player.getAbilities().creativeMode) {
            error("Creative mode only.");
            this.toggle();
        }
    }
    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (!toggleOnLog.get()) return;
        toggle();
    }
    @EventHandler
    private void onPlaySound(PlaySoundEvent event) {
        if (event.sound.getId().toString().startsWith("minecraft:item.armor.equip") && blockSound.get()) {
            event.cancel();
        }
    }
    @EventHandler
    private void onTick(TickEvent.Post event) {
        ticks++;
        if (ticks % headDelay.get() == 0) {
            List<Item> selectedItems = new ArrayList<>(items.get());
            Collections.shuffle(selectedItems, new Random());
            if (selectedItems.isEmpty()) return;
            ItemStack itemStack = new ItemStack(selectedItems.get(0 % selectedItems.size()));

            SetItem.set(itemStack, 5);
        }
    }
}
