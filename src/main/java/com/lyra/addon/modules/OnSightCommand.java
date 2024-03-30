package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class OnSightCommand extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General
    private final Setting<List<String>> messages = sgGeneral.add(new StringListSetting.Builder()
        .name("message")
        .description("The specified message sent to the server.")
        .defaultValue("/msg %player% hi")
        .build()
    );
    private final Setting<Boolean> isTeleport = sgGeneral.add(new BoolSetting.Builder()
        .name("silent-tp")
        .description("Silently teleports to the player.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> range = sgTargeting.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum range.")
        .defaultValue(20)
        .min(0)
        .sliderMax(50)
        .build()
    );

    private final Setting<Double> wallsRange = sgTargeting.add(new DoubleSetting.Builder()
        .name("walls-range")
        .description("The maximum range through walls.")
        .defaultValue(0)
        .min(0)
        .sliderMax(50)
        .build()
    );


    private final Setting<Integer> maxTargets = sgTargeting.add(new IntSetting.Builder()
        .name("max-targets")
        .description("How many entities to target at once.")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 5)
        .build()
    );

    private final Setting<Boolean> isRender = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Renders the radius.")
        .defaultValue(false)
        .build()
    );
    private final Setting <SettingColor> renderColor = sgRender.add(new ColorSetting.Builder()
        .name("render-color")
        .description("Block render color.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );

    private final List<Entity> targets = new ArrayList<>();

    public OnSightCommand() {
        super(Addon.CATEGORY, "on-sight-command", "Executes commands on players on sight.");
    }
    @Override
    public void onDeactivate() {
        targets.clear();
    }

    String regex = "[A-Za-z0-9_]+";

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!mc.player.isAlive()) return;
        TargetUtils.getList(targets, this::entityCheck, SortPriority.ClosestAngle, maxTargets.get());
        if (!mc.options.attackKey.isPressed()) return;
        targets.forEach(this::start);
    }

    private boolean entityCheck(Entity entity) {
        if (entity.equals(mc.player) || entity.equals(mc.cameraEntity)) return false;
        if ((entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) || !entity.isAlive()) return false;
        if (!PlayerUtils.isWithin(entity, range.get())) return false;
        if (!PlayerUtils.canSeeEntity(entity) && !PlayerUtils.isWithin(entity, wallsRange.get())) return false;
        if (!Pattern.matches(regex, entity.getName().getString())) return false;
        return entity.isPlayer();
    }

    private void start(Entity target) {
        double tx = target.getX(), ty = target.getY(), tz = target.getZ();
        if (isTeleport.get()) warpPlayer(mc.player.getX(), mc.player.getY(), mc.player.getZ(), tx, ty, tz);
        for (String msg : messages.get()) {
            ChatUtils.sendPlayerMsg(msg.replaceAll("%player%", target.getName().getString()));
        }
        if (isTeleport.get()) warpPlayer(mc.player.getX(), mc.player.getY(), mc.player.getZ(), tx, ty, tz);
    }

    private void warpPlayer(double x1, double y1, double z1, double x2, double y2, double z2) {

        double distance = calculateDistance(x1, y1, z1, x2, y2, z2);
        int packetsRequired = (int) Math.ceil(Math.abs(distance / 10));

        for (int packetNumber = 0; packetNumber < (packetsRequired - 1); packetNumber++) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
        }

        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x2, y2, z2, true));
    }

    public static double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public Entity getTarget() {
        if (!targets.isEmpty()) return targets.get(0);
        return null;
    }

    @Override
    public String getInfoString() {
        if (!targets.isEmpty()) return EntityUtils.getName(getTarget());
        return null;
    }
    @EventHandler
    private void onKey(KeyEvent event) {
        if (mc.options.useKey.matchesKey(event.key, 0) || mc.options.useKey.matchesMouse(event.key)) {
            System.out.println("test");
        }
    }
    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (isRender.get()) {
            for (Entity entity : targets) {
                drawBoundingBox(event, entity);
            }
        }
    }

    private void drawBoundingBox(Render3DEvent event, Entity entity) {
        double x = MathHelper.lerp(event.tickDelta, entity.lastRenderX, entity.getX()) - entity.getX();
        double y = MathHelper.lerp(event.tickDelta, entity.lastRenderY, entity.getY()) - entity.getY();
        double z = MathHelper.lerp(event.tickDelta, entity.lastRenderZ, entity.getZ()) - entity.getZ();

        Box box = entity.getBoundingBox();
        event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, renderColor.get(), renderColor.get(), ShapeMode.Lines, 0);
    }

}
