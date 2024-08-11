package com.lyra.addon.commands;

import com.lyra.addon.utils.SetItem;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;

public class StackCommand extends Command {
    public StackCommand() {
        super("stack", "Gives you a stack of the item you're holding.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            givePlayerHeldItemStack(64);
            return SINGLE_SUCCESS;
        }).then(argument("amount", IntegerArgumentType.integer()).executes(context -> {
            int stackAmount = IntegerArgumentType.getInteger(context, "amount");
            givePlayerHeldItemStack(stackAmount);
            return SINGLE_SUCCESS;
        })).then(literal("half").executes(context -> {
            givePlayerHeldItemStack(32);
            return SINGLE_SUCCESS;
        })).then(literal("quarter").executes(context -> {
            givePlayerHeldItemStack(16);
            return SINGLE_SUCCESS;
        }));

    }

    private void givePlayerHeldItemStack(int stackAmount) {
        if (!mc.player.getAbilities().creativeMode) {
            error("Creative mode only.");
            return;
        }
        if(stackAmount > 64) {
            error("Amount can't be bigger than 64.");
            return;
        }
        ItemStack heldItemStack = mc.player.getMainHandStack().copy();
        if (!heldItemStack.isEmpty()) {
            ItemStack itemStack = heldItemStack.copy();
            itemStack.setCount(stackAmount);
            SetItem.setMainHand(itemStack);
        }
    }
}
