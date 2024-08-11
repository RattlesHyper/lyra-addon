package com.lyra.addon.commands;

import com.lyra.addon.utils.arguments.SpawnEggArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.ComponentMapArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemStack;
import com.lyra.addon.utils.SetItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class SummonCommand extends Command {
    private final static SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType(Text.literal("You must be in creative mode to use this."));
    public SummonCommand() {
        super("summon", "Tries to summon entities silently with spawn eggs.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("name", SpawnEggArgumentType.get()).executes(context -> {
            ItemStack oldItem = mc.player.getOffHandStack();
            if (!mc.player.getAbilities().creativeMode) throw NOT_IN_CREATIVE.create();
            ItemStack item = SpawnEggArgumentType.getItemStack(context, "name").createStack(1, false);
            start(item, 1);
            return SINGLE_SUCCESS;
        }).then(argument("number", IntegerArgumentType.integer()).executes(context -> {
            if (!mc.player.getAbilities().creativeMode) throw NOT_IN_CREATIVE.create();
            ItemStack item = SpawnEggArgumentType.getItemStack(context, "name").createStack(1, false);
            start(item, IntegerArgumentType.getInteger(context, "number"));
            return SINGLE_SUCCESS;
        }).then(argument("components", ComponentMapArgumentType.componentMap(REGISTRY_ACCESS)).executes(context -> {
            ComponentMap comps = ComponentMapArgumentType.getComponentMap(context, "components");
            ItemStack item = SpawnEggArgumentType.getItemStack(context, "name").createStack(1, false);
            item.applyComponentsFrom(comps);
            start(item, IntegerArgumentType.getInteger(context, "number"));
            return SINGLE_SUCCESS;
        }))));
    }

    private void start(ItemStack egg, Integer count) {
        ItemStack oldItem = mc.player.getOffHandStack();
        SetItem.setOffHand(egg);
        for (int i = 0; i < count; i++) {
            place();
        }
        SetItem.setOffHand(oldItem);
    }

    private void place() {
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
