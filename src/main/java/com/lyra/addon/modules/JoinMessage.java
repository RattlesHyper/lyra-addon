package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;

import java.util.*;

public class JoinMessage extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay in ms before executing the command.")
        .defaultValue(1000)
        .min(0)
        .sliderMax(10000)
        .build()
    );
    private final Setting<List<String>> messages = sgGeneral.add(new StringListSetting.Builder()
        .name("messages")
        .description("Messages to send on player join.")
        .defaultValue(List.of("(me) just joined the server."))
        .build()
    );
    public JoinMessage() {
        super(Addon.CATEGORY, "join-message", "Runs command when you spawn in a server. Put (me) to put username automatically.");
    }
    private final Timer timer = new Timer();
    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        if (!isActive()) return;
        if (messages.get().isEmpty()) return;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mc.player != null) {
                    for (String msg : messages.get()) {
                        ChatUtils.sendPlayerMsg(msg.replaceAll("(me)", String.valueOf(mc.player.getName())));
                    }
                }
            }
        }, delay.get());
    }
}
