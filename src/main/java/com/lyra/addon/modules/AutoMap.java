package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoMap extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgDebug = settings.createGroup("Debug");
    public Setting<BlockPos> posOne = sgGeneral.add(new BlockPosSetting.Builder()
        .name("start-position")
        .description("The location of the start.")
        .defaultValue(BlockPos.ORIGIN)
        .build()
    );
    public Setting<BlockPos> posTwo = sgGeneral.add(new BlockPosSetting.Builder()
        .name("end-position")
        .description("The location of the end.")
        .defaultValue(BlockPos.ORIGIN)
        .build()
    );
    private final Setting<Integer> whatDelay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .defaultValue(1)
        .sliderRange(1, 20)
        .build()
    );
    private final Setting < Boolean > isDetectBlock = sgGeneral.add(new BoolSetting.Builder()
        .name("detect-block")
        .description("Detects if theres any blocks under before moving to the next block.")
        .defaultValue(false)
        .build()
    );
    private final Setting < Boolean > isLookAt = sgGeneral.add(new BoolSetting.Builder()
        .name("look-at")
        .description("Looks at the block under you.")
        .defaultValue(false)
        .build()
    );
    private final Setting < Boolean > isAutoToggle = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-disable")
        .description("Disables the module when it cant find the next block.")
        .defaultValue(false)
        .build()
    );
    private final Setting < Boolean > isGamemode = sgGeneral.add(new BoolSetting.Builder()
        .name("detect-game-mode")
        .description("Disables the module when you're not in creative.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> isRender = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Renders the radius.")
        .defaultValue(false)
        .build()
    );
    private final Setting<ShapeMode> renderMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("render-mode")
        .description("Render mode.")
        .defaultValue(ShapeMode.Lines)
        .build()
    );
    private final Setting <SettingColor> renderColor = sgRender.add(new ColorSetting.Builder()
        .name("render-color")
        .description("Block render color.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting < Boolean > isLog = sgDebug.add(new BoolSetting.Builder()
        .name("enable-logs")
        .description("Show logs in chat.")
        .defaultValue(false)
        .build()
    );

    public AutoMap() {
        super(Addon.CATEGORY, "auto-map", "Helps you build 2d schematics.");
    }
    private boolean showBox;
    private int ticks;
    @Override
    public void onActivate() {
        showBox = true;
    }
    @Override
    public void onDeactivate() {
        showBox = false;
    }
    @EventHandler
    private void onTick(TickEvent.Post event) {

        ticks++;
        if (ticks % whatDelay.get() == 0) {
        if (!mc.player.getAbilities().creativeMode && isGamemode.get()) {
            this.toggle();
            error("Disabled because player not in creative mode.");
        }

        int[] nextPoint = getNextPoint(mc.player.getBlockX(), mc.player.getBlockZ(), posOne.get().getX(), posOne.get().getZ(), posTwo.get().getX(), posTwo.get().getZ());

        if (nextPoint != null) {
            if(isDetectBlock.get()) {
            if(BlockUtils.getPlaceSide(mc.player.getBlockPos().down()) != null) {
                mc.player.updatePosition(nextPoint[0] + 0.5, mc.player.getY(), nextPoint[1] + 0.5);
            }
            } else {
                mc.player.updatePosition(nextPoint[0] + 0.5, mc.player.getY(), nextPoint[1] + 0.5);
            }
            if(isLookAt.get()) {
                mc.player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, new Vec3d(mc.player.getX() , mc.player.getBlockY() , mc.player.getZ()));
            }
        } else {
            if (isLog.get() && isAutoToggle.get()) {
                info("Cant find next block. toggled §coff§7.");
                this.toggle();
            } else if (isAutoToggle.get()) {
                this.toggle();
            }
        }
        }
    }
    @EventHandler
    private void onRender(Render3DEvent event) {
        if (showBox && isRender.get()) {
            event.renderer.box(posOne.get().getX(), posOne.get().getY() - 1, posOne.get().getZ(), posTwo.get().getX() + 1, posOne.get().getY(), posTwo.get().getZ() + 1, renderColor.get(), renderColor.get(), renderMode.get(), 0);
        } else event.renderer.end();
    }

    public static int[] getNextPoint(int currentX, int currentZ, int startX, int startZ, int endX, int endZ) {
        if (currentX < startX || currentX > endX || currentZ < startZ || currentZ > endZ) {
            return null;
        }
        if (currentZ % 2 == 0) {
            if (currentX < endX) {
                return new int[]{currentX + 1, currentZ};
            } else if (currentZ < endZ) {
                return new int[]{endX, currentZ + 1};
            }
        } else {
            if (currentX > startX) {
                return new int[]{currentX - 1, currentZ};
            } else if (currentZ < endZ) {
                return new int[]{startX, currentZ + 1};
            }
        }
        return null;
    }

}
