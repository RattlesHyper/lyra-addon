package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class PacketScaffold extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final Setting<Boolean> isYlock = sgGeneral.add(new BoolSetting.Builder()
        .name("y-lock")
        .description("Locks the Y position from the starting Y position.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> isInsideBlock = sgGeneral.add(new BoolSetting.Builder()
        .name("inside-block")
        .description("Inside block true.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Direction> clickDirection = sgGeneral.add(new EnumSetting.Builder<Direction>()
        .name("block-direction")
        .description("The side of the block to click.")
        .defaultValue(Direction.UP)
        .build()
    );
    private final Setting<Hand> handMode = sgGeneral.add(new EnumSetting.Builder<Hand>()
        .name("hand")
        .description("Set hand.")
        .defaultValue(Hand.MAIN_HAND)
        .build()
    );
    private final Setting<Boolean> isRender = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Renders the block.")
        .defaultValue(false)
        .build()
    );
    private final Setting<ShapeMode> renderMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("render-mode")
        .description("Render mode.")
        .defaultValue(ShapeMode.Lines)
        .build()
    );
    private final Setting < SettingColor > renderColor = sgRender.add(new ColorSetting.Builder()
        .name("render-color")
        .description("Block render color.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    public PacketScaffold() {
        super(Addon.CATEGORY, "packet-scaffold", "Scaffold that uses packets.");
    }
    double ylock = 0;
    private boolean showBox;
    @Override
    public void onActivate() {
        ylock = mc.player.getY() - 1;
        showBox = true;
    }
    @Override
    public void onDeactivate() {
        showBox = false;
    }
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        BlockPos customPos = new BlockPos((int) mc.player.getPos().x, (int) (isYlock.get() ? ylock : mc.player.getY() -1), (int) mc.player.getZ());
        Hand customHand = handMode.get();
        BlockHitResult customHitResult = new BlockHitResult(
            new Vec3d(customPos.getX(), customPos.getY(), customPos.getZ()),
            clickDirection.get(),
            customPos,
            isInsideBlock.get()
        );
        PlayerInteractBlockC2SPacket customPacket = new PlayerInteractBlockC2SPacket(customHand, customHitResult, 0);
        mc.player.networkHandler.sendPacket(customPacket);
    }
    @EventHandler
    private void onRender(Render3DEvent event) {
        if (showBox && isRender.get()) {
        event.renderer.box(mc.player.getBlockX() +1, isYlock.get() ? ylock : mc.player.getBlockY() -1, mc.player.getBlockZ() + 1, mc.player.getBlockX() , isYlock.get() ? ylock +1 : mc.player.getBlockY(), mc.player.getBlockZ(), renderColor.get(), renderColor.get(), renderMode.get(), 0);
        } else event.renderer.end();
    }

}
