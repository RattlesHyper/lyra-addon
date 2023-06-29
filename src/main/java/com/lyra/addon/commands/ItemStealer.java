package com.lyra.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ItemStealer extends Command {
    public ItemStealer() {
        super("steal", "Steals the targets held item. Creative mode only.");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("player", PlayerArgumentType.create()).executes(context -> {
            PlayerEntity player = PlayerArgumentType.get(context);
            ItemStack stack = player.getInventory().getMainHandStack();
            System.out.println(stack);
            giveItem(stack);
            return SINGLE_SUCCESS;
        }));
    }
    private void giveItem(ItemStack itemStack) {
        if (!mc.player.getAbilities().creativeMode) {
            error("Creative mode only.");
            return;
        }
        mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36 + mc.player.getInventory().selectedSlot, itemStack));
    }
}
