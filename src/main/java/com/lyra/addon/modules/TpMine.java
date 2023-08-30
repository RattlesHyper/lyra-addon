package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class TpMine extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> maxDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("max-distance")
        .description("The maximum distance you can teleport.")
        .defaultValue(20)
        .min(0)
        .build()
    );
    private final Setting<Double> stepSize = sgGeneral.add(new DoubleSetting.Builder()
        .name("step-size")
        .description("Blocks to travel every step to the target.")
        .defaultValue(4)
        .min(1)
        .sliderMax(10)
        .build()
    );

    private HitResult hitResult;
    private BlockPos pos;
    private int hitDelayTimer;
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
                if (calculateDistance(((BlockHitResult) hitResult).getBlockPos().getX(),((BlockHitResult) hitResult).getBlockPos().getY(), ((BlockHitResult) hitResult).getBlockPos().getZ(), mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ()) < mc.interactionManager.getReachDistance()) return;
                findPath(mc.player.getX(), mc.player.getY(), mc.player.getZ(), pos.getX() + 0.5 + side.getOffsetX(), pos.getY() + height, pos.getZ() + 0.5 + side.getOffsetZ());
                BlockUtils.breakBlock(((BlockHitResult) hitResult).getBlockPos(), true);
                findPath(pos.getX() + 0.5 + side.getOffsetX(), pos.getY() + height, pos.getZ() + 0.5 + side.getOffsetZ(), mc.player.getX(), mc.player.getY(), mc.player.getZ());
            }

        }
    }

    private void findPath(double x1, double y1, double z1, double x2, double y2, double z2) {

        double distance = calculateDistance(x1, y1, z1, x2, y2, z2);

        int totalSteps = (int) Math.ceil(distance / stepSize.get());

        double dx = (x2 - x1) / totalSteps;
        double dy = (y2 - y1) / totalSteps;
        double dz = (z2 - z1) / totalSteps;

        for (int i = 0; i < totalSteps; i++) {
            double currentX = x1 + i * dx;
            double currentY = y1 + i * dy;
            double currentZ = z1 + i * dz;

            tpPacket(currentX, currentY, currentZ);
        }
    }

    public static double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private void tpPacket(double x, double y, double z) {
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true));
    }
}
