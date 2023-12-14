package com.lyra.addon.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class CreativeSetItem {
    public static void set(ItemStack item, int slot) {
        CreativeInventoryActionC2SPacket packet = new CreativeInventoryActionC2SPacket(slot, item);
        mc.player.networkHandler.sendPacket(packet);
    }
}
