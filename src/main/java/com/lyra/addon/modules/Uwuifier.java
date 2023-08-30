package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import com.lyra.addon.utils.Uwuify;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

    public class Uwuifier extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> addEmoji = sgGeneral.add(new BoolSetting.Builder()
        .name("add-emoji")
        .description("Adds an emoji to the end.")
        .defaultValue(true)
        .build()
    );

    public Uwuifier() {
        super(Addon.CATEGORY, "uwuifier", "Uwuifies your messages.");
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        String message = event.message;

        event.message = Uwuify.uwuify(message, addEmoji.get());
    }
}
