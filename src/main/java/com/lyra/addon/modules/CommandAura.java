package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;

import java.util.List;
import java.util.regex.*;

public class CommandAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDebug = settings.createGroup("Debug");

    private final Setting<List<String>> messages = sgGeneral.add(new StringListSetting.Builder()
        .name("message")
        .description("The specified message sent to the server.")
        .defaultValue("/msg %player% hi")
        .build()
    );
    private final Setting<Target> targetMode = sgGeneral.add(new EnumSetting.Builder<Target>()
        .name("target")
        .description("Only targets selected target.")
        .defaultValue(Target.Everyone)
        .build()
    );
    private final Setting<Boolean> toggleOnDeath = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-on-death")
        .description("Disables when you die.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> toggleOnLog = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-on-log")
        .description("Disables when you disconnect from a server.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> isLogs = sgDebug.add(new BoolSetting.Builder()
        .name("enable-logs")
        .description("Show logs in chat.")
        .defaultValue(false)
        .build()
    );

    public CommandAura() {
        super(Addon.CATEGORY, "command-aura", "Sends a message when players come in render distance.");
    }
    String regex = "[A-Za-z0-9_]+";
    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (!toggleOnLog.get()) return;
        toggle();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event)  {
        if (event.packet instanceof DeathMessageS2CPacket packet) {
            Entity entity = mc.world.getEntityById(packet.getEntityId());
            if (entity == mc.player && toggleOnDeath.get()) {
                toggle();
                info("Toggled off because you died.");
            }
        }
    }
    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        if (!(event.entity instanceof PlayerEntity) || event.entity.getUuid().equals(mc.player.getUuid())) return;
        if (!Pattern.matches(regex, event.entity.entity.getName().getString())) return;
        String targetName = event.entity.getName().getString();


        if (targetMode.get() == Target.Everyone) {
            for (String msg : messages.get()) {
                ChatUtils.sendPlayerMsg(msg.replaceAll("%player%", targetName));
            }
            if(isLogs.get()) {
                info("Used command on §a" + targetName + "§7.");
            }
        }
        if (targetMode.get() == Target.OnlyFriends && Friends.get().isFriend((PlayerEntity)event.entity)) {
            for (String msg : messages.get()) {
                ChatUtils.sendPlayerMsg(msg.replaceAll("%player%", targetName));
            }
            if(isLogs.get()) {
                info("Used command on §a" + targetName + "§7.");
            }
        }
        if (targetMode.get() == Target.IgnoreFriends && !Friends.get().isFriend((PlayerEntity)event.entity)) {
            for (String msg : messages.get()) {
                ChatUtils.sendPlayerMsg(msg.replaceAll("%player%", targetName));
            }
            if(isLogs.get()) {
                info("Used command on §a" + targetName + "§7.");
            }
        }
    }
    public enum Target {
        Everyone,
        OnlyFriends,
        IgnoreFriends
    }

}
