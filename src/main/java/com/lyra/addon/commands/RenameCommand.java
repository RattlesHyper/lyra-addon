package com.lyra.addon.commands;

import com.lyra.addon.utils.SetItem;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class RenameCommand extends Command {
    public RenameCommand() {
        super("rename", "Renames the held item in creative mode.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            setCustomName("");
            return SINGLE_SUCCESS;
        }).then(argument("name", StringArgumentType.string()).executes(context -> {
            String customName = StringArgumentType.getString(context, "name");
            setCustomName(customName);
            return SINGLE_SUCCESS;
        }));

    }

    private void setCustomName(String customName) {
        if (!mc.player.getAbilities().creativeMode) {
            error("Creative mode only.");
            return;
        }
        ItemStack heldItemStack = mc.player.getInventory().getMainHandStack().copy();

        if (!heldItemStack.isEmpty()) {
            ItemStack itemStack = heldItemStack.copy();
            itemStack.remove(DataComponentTypes.CUSTOM_DATA);
            itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(customName.replaceAll("&", "§")));
            SetItem.setMainHand(itemStack);
        }

    }
}
