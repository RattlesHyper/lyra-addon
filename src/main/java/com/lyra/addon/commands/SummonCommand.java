package com.lyra.addon.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SummonCommand extends Command {
    private final static SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType(Text.literal("You must be in creative mode to use this."));
    private final static SimpleCommandExceptionType NO_SPACE = new SimpleCommandExceptionType(Text.literal("No space in hotbar."));
    public SummonCommand() {
        super("summon", "Gives you a stack of the item you're holding.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("item", ItemStackArgumentType.itemStack(REGISTRY_ACCESS)).executes(context -> {
            ItemStack oldItem = mc.player.getOffHandStack();
            if (!mc.player.getAbilities().creativeMode) throw NOT_IN_CREATIVE.create();
            ItemStack item = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false);
            mc.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(45, item));
            placeItem();
            mc.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(45, oldItem));
            return SINGLE_SUCCESS;
        }).then(argument("number", IntegerArgumentType.integer()).executes(context -> {
            if (!mc.player.getAbilities().creativeMode) throw NOT_IN_CREATIVE.create();
            ItemStack item = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false);
            mc.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(45, item));
            ItemStack oldItem = mc.player.getOffHandStack();
            for (int i = 0; i < IntegerArgumentType.getInteger(context, "number"); i++) {
                placeItem();
            }
            mc.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(45, oldItem));
            return SINGLE_SUCCESS;
        })));
    }

    private void placeItem() {
        BlockPos customPos = new BlockPos(mc.player.getBlockPos().offset(Direction.DOWN));
        BlockHitResult customHitResult = new BlockHitResult(
            new Vec3d(customPos.getX(), customPos.getY(), customPos.getZ()),
            Direction.UP,
            customPos,
            true
        );
        PlayerInteractBlockC2SPacket customPacket = new PlayerInteractBlockC2SPacket(Hand.OFF_HAND, customHitResult, 0);
        mc.player.networkHandler.sendPacket(customPacket);
    }
}
