package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import com.mojang.authlib.GameProfile;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Position;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

public class Stick extends Module{

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> targetMode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("target-mode")
        .description("The mode at which to follow the player.")
        .defaultValue(Mode.Automatic)
        .onChanged(onChanged -> {
            target = null;
        })
        .build()
    );
    private final Setting<Follow> followMode = sgGeneral.add(new EnumSetting.Builder<Follow>()
        .name("follow")
        .description("Which parts rotation to follow.")
        .defaultValue(Follow.Head)
        .build()
    );
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum range to set target.")
        .defaultValue(6)
        .min(0)
        .sliderMax(6)
        .build()
    );
    private final Setting<Vector3d> offset = sgGeneral.add(new Vector3dSetting.Builder()
        .name("offset")
        .description("Offset from target.")
        .defaultValue(0, 0, 0)
        .sliderRange(-3, 3)
        .decimalPlaces(1)
        .build()
    );

    public Stick() {
        super(Addon.CATEGORY, "stick", "Stick to a player.");
    }
    private final List<Entity> targets = new ArrayList<>();
    Entity target = null;

    @Override
    public void onActivate() {
        if(targetMode.get() == Mode.Automatic){
            setTarget();
        }
    }

    private boolean entityCheck(Entity entity) {
        if (entity.equals(mc.player) || entity.equals(mc.cameraEntity)) return false;
        if ((entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) || !entity.isAlive()) return false;
        if (!PlayerUtils.isWithin(entity, range.get())) return false;
        if (!PlayerUtils.canSeeEntity(entity) && !PlayerUtils.isWithin(entity, range.get())) return false;
        return entity.isPlayer();
    }

    //middle click mode
    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if(targetMode.get() == Mode.MiddleClick){
            if (event.action == KeyAction.Press && event.button == GLFW_MOUSE_BUTTON_MIDDLE && mc.currentScreen == null) {
                if (mc.targetedEntity instanceof PlayerEntity) {
                    target = mc.targetedEntity;
                } else  {
                    target = null;
                    mc.player.getAbilities().flying = false;
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (target == null) return;
        checkEntity();
        mc.player.getAbilities().flying = true;

        switch (followMode.get()) {
            case Head -> {
                Rotations.rotate(Rotations.getYaw(target), 0);
                Position head = target.raycast(-1 + offset.get().z, 1f / 20f, false).getPos();
                mc.player.setPosition(head.getX() + offset.get().x, head.getY() + offset.get().y, head.getZ());
            }
            case Body -> {
                Position head = target.raycast(0.5, 1f / 20f, false).getPos();
                mc.player.setPosition(target.getX() + offset.get().x, target.getY() + offset.get().y, target.getZ() + offset.get().z);
            }
        }
    }

    private void checkEntity() {
        List <String> playerNamesList = mc.player.networkHandler.getPlayerList().stream()
            .map(PlayerListEntry::getProfile)
            .map(GameProfile::getName)
            .toList();

        if (!playerNamesList.contains(EntityUtils.getName(target)) && targetMode.get() == Mode.Automatic) {
            target = null;
        }

        if (target == null && targetMode.get() == Mode.Automatic) {
            setTarget();
        }
    }

    @Override
    public void onDeactivate() {
        target = null;
        mc.player.getAbilities().flying = false;
    }
    public void setTarget() {
        TargetUtils.getList(targets, this::entityCheck, SortPriority.LowestDistance, 1);
        if(targets.isEmpty()) return;
        target = targets.get(0);
    }
    public enum Mode {
        MiddleClick,
        Automatic
    }
    public enum Follow {
        Head,
        Body
    }
}
