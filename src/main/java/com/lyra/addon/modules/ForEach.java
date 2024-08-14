package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.client.network.PlayerListEntry;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import java.util.regex.Pattern;
import java.util.List;

public class ForEach extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDebug = settings.createGroup("Debug");
    private final Setting<List<String>> messages = sgGeneral.add(new StringListSetting.Builder()
        .name("message")
        .description("The specified message sent to the server.")
        .defaultValue("/msg %target% hi from %me%")
        .build()
    );
    private final Setting<Integer> delayTime = sgGeneral.add(new IntSetting.Builder()
        .name("delay-in-ms")
        .defaultValue(1000)
        .sliderRange(1, 2000)
        .build()
    );
    private final Setting<Boolean> isLoop = sgGeneral.add(new BoolSetting.Builder()
        .name("loop")
        .description("Keeps looping after finishing.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Will not target friends.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> ignoreSelf = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-self")
        .description("Will not target your name.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> toggleOnDeath = sgGeneral.add(new BoolSetting.Builder()
        .name("disable-on-death")
        .description("Disables when you die.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> toggleOnLog = sgGeneral.add(new BoolSetting.Builder()
        .name("disable-on-log")
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
    public ForEach() {
        super(Addon.CATEGORY, "for-each", "Execute command on all players.");
    }

    private ExecutorService executor;
    private volatile boolean stopFlag;
    String regex = "[A-Za-z0-9_]+";

    public void startProcessing() {
        stopProcessing();

        executor = Executors.newSingleThreadExecutor();
        executor.execute(this::printPlayerNamesWithDelay);
    }

    public void stopProcessing() {
        stopFlag = true;
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public void printPlayerNamesWithDelay() {
        stopFlag = false;

        List <String> playerNamesList = mc.player.networkHandler.getPlayerList().stream()
            .map(PlayerListEntry::getProfile)
            .map(GameProfile::getName)
            .toList();

        for (String playerName: playerNamesList) {
            if (stopFlag) {
                return;
            }
            try {
                if (EntityUtils.getName(mc.player).equals(playerName) && ignoreSelf.get()) continue;
                if (!ignoreFriends.get() || ignoreFriends.get() && Friends.get().get(playerName) == null) {
                    boolean isMatch = Pattern.matches(regex, playerName);
                    if (isMatch) {
                        for (String msg : messages.get()) {
                            ChatUtils.sendPlayerMsg(msg.replaceAll("%target%", playerName).replaceAll("%me%", EntityUtils.getName(mc.player)));
                        }
                        if (isLogs.get()) {
                            info("Used command on §a" + playerName + "§7.");
                        }
                    }
                }
                Thread.sleep(delayTime.get());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (isLoop.get()) {
            startProcessing();
            if (isLogs.get()) {
                info("Restarting the loop.");
            }
        } else {
            this.toggle();
            if (isLogs.get()) {
                info("Used command on all possible players. toggled §coff§7.");
            }
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (!toggleOnLog.get()) return;
        toggle();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event)  {
        if (event.packet instanceof DeathMessageS2CPacket packet) {
            Entity entity = mc.world.getEntityById(packet.playerId());
            if (entity == mc.player && toggleOnDeath.get()) {
                toggle();
                info("Toggled off because you died.");
            }
        }
    }

    @Override
    public void onActivate() {
        startProcessing();
    }
    @Override
    public void onDeactivate() {
        stopProcessing();
    }

}
