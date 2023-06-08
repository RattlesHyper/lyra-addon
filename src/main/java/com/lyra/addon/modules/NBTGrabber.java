package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.LinkedList;
import java.util.List;


public class NBTGrabber extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> ignoreSelf = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-self")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> allItems = sgGeneral.add(new BoolSetting.Builder()
        .name("all-items")
        .defaultValue(false)
        .build()
    );
    private final Setting<ListMode> itemsFilter = sgGeneral.add(new EnumSetting.Builder<ListMode>()
        .name("items-filter")
        .defaultValue(ListMode.Whitelist)
        .visible(() -> !allItems.get())
        .build()
    );
    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .defaultValue(Items.COMPASS)
        .visible(() -> !allItems.get())
        .build()
    );
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .defaultValue(Mode.GetRequiredTags)
        .build()
    );
    private final Setting<List<String>> requiredTags = sgGeneral.add(new StringListSetting.Builder()
        .name("required-tags")
        .defaultValue("LodestonePos")
        .visible(() -> mode.get() == Mode.GetRequiredTags)
        .build()
    );
    private final Setting<Integer> cooldownCheck = sgGeneral.add(new IntSetting.Builder()
        .name("cooldown-check-in-ticks")
        .defaultValue(1)
        .sliderRange(1, 600)
        .build()
    );
    private final Setting<Boolean> toggleOnDeath = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-on-death")
        .description("Disables grabber when you die.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> toggleOnLog = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-on-log")
        .description("Disables grabber when you disconnect from a server.")
        .defaultValue(true)
        .build()
    );
    public NBTGrabber() {
        super(Addon.CATEGORY, "nbt-grabber", "Generates selected items and shows them in the chat.");
    }

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
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof DeathMessageS2CPacket packet) {
            Entity entity = mc.world.getEntityById(packet.getEntityId());
            if (entity == mc.player && toggleOnDeath.get()) {
                toggle();
                info("Toggled off because you died.");
            }
        }
    }
    private int ticks;
    private LinkedList<NbtCompound> tags = new LinkedList<>();
    @EventHandler
    public void onTick(TickEvent.Post event) {
        ticks++;
        if (ticks % cooldownCheck.get() == 0) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player.isMainPlayer() && ignoreSelf.get()) continue;
                for (ItemStack stack : player.getItemsEquipped()) {
                    if (!stack.hasNbt()) continue;

                    Item item = stack.getItem();

                    if (!allItems.get()) {
                        if (items.get().isEmpty()) return;
                        if ((itemsFilter.get() == ListMode.Whitelist && !items.get().contains(item))
                            || (itemsFilter.get() == ListMode.Blacklist && items.get().contains(item))
                        ) continue;
                    }

                    NbtCompound tag = null;

                    if (mode.get() == Mode.GetNBT) tag = stack.getNbt();
                    else {
                        if (requiredTags.get().isEmpty()) return;
                        for (String requireTag : requiredTags.get()) {
                            if (stack.getNbt().contains(requireTag)) {
                                tag = (NbtCompound) stack.getNbt().get(requireTag);
                            }
                        }
                        if (tag == null) continue;
                    }

                    if (!tags.contains(tag)) tags.add(tag);
                    else continue;

                    MutableText copyButton = Text.literal("NBT");
                    copyButton.setStyle(copyButton.getStyle()
                        .withFormatting(Formatting.UNDERLINE)
                        .withClickEvent(new ClickEvent(
                            ClickEvent.Action.COPY_TO_CLIPBOARD,
                            tag.toString()
                        ))
                        .withHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Text.literal("Copy the NBT data to your clipboard.")
                        ))
                    );

                    info(Text.literal(
                            player.getEntityName() + " has " + item.toString() + " with "
                        ).append(copyButton).append(" ").append(NbtHelper.toPrettyPrintedText(tag))
                    );
                }
            }
        }
    }
    public enum Mode {
        GetNBT,
        GetRequiredTags
    }
    public enum ListMode {
        Whitelist,
        Blacklist
    }
}


