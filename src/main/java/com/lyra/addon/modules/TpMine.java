package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import com.lyra.addon.utils.WarpExploit;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class TpMine extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Double> maxDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("max-distance")
        .description("The maximum distance you can teleport.")
        .defaultValue(20)
        .min(0)
        .build()
    );
    private final Setting<Boolean> isRenderBlock = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Renders the target block.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SettingColor> targetColor = sgRender.add(new ColorSetting.Builder()
        .name("render-color")
        .description("Set target block render color.")
        .defaultValue(new SettingColor(0, 255, 150, 255))
        .visible(isRenderBlock::get)
        .build()
    );

    private HitResult hitResult;
    private BlockPos pos;
    public TpMine() {
        super(Addon.CATEGORY, "tp-mine", "Teleports you to the block silently and breaks it.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        hitResult = mc.player.raycast(maxDistance.get(), 1f / 20f, false);
        pos = ((BlockHitResult) hitResult).getBlockPos();

        if (mc.options.attackKey.isPressed()) {
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                Direction side = ((BlockHitResult) hitResult).getSide();
                BlockState state = mc.world.getBlockState(pos);
                VoxelShape shape = state.getCollisionShape(mc.world, pos);
                if (shape.isEmpty()) shape = state.getOutlineShape(mc.world, pos);
                double height = shape.isEmpty() ? 1 : shape.getMax(Direction.Axis.Y);
                double tx = pos.getX() + 0.5 + side.getOffsetX(), ty = pos.getY() + height, tz = pos.getZ() + 0.5 + side.getOffsetZ();

                WarpExploit.warp(mc.player.getX(), mc.player.getY(), mc.player.getZ(), tx, ty, tz);
                BlockUtils.breakBlock(((BlockHitResult) hitResult).getBlockPos(), true);
                WarpExploit.warp(tx, ty, tz, mc.player.getX(), mc.player.getY(), mc.player.getZ());
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!isRenderBlock.get()) return;
        if (pos == null || hitResult.getType() != HitResult.Type.BLOCK) return;
        RenderUtils.renderTickingBlock(pos, targetColor.get(), targetColor.get(), ShapeMode.Lines, 0, 0, false, false);
    }
}
