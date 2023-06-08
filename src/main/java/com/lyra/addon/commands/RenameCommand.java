package com.lyra.addon.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.systems.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

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
        ItemStack heldItemStack = mc.player.getMainHandStack().copy();
        if (!heldItemStack.isEmpty()) {
            ItemStack itemStack = heldItemStack.copy();
            itemStack.setCustomName(Text.of(customName.replaceAll("&", "§")));
            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36 + mc.player.getInventory().selectedSlot, itemStack));
        }
    }
}
