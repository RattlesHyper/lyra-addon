package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import com.lyra.addon.utils.Uwuify;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class Uwuifier extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> faces = sgGeneral.add(new BoolSetting.Builder()
        .name("add-faces")
        .description("Adds random faces E.g. UwU, OwO.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> expression = sgGeneral.add(new BoolSetting.Builder()
        .name("add-expression")
        .description("Adds random expressions E.g. meow, nya~.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> actions = sgGeneral.add(new BoolSetting.Builder()
        .name("add-actions")
        .description("Adds random actions E.g. *cries*, *blushes*.")
        .defaultValue(true)
        .build()
    );

    public Uwuifier() {
        super(Addon.CATEGORY, "uwuifier", "Uwuifies your messages.");
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        String message = event.message;

        event.message = Uwuify.uwuify(message, faces.get(), expression.get(), actions.get());
    }
}
