package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import com.lyra.utils.Uwuify;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.awt.*;
import java.util.Random;

public class Uwuifier extends Module {

    public Uwuifier() {
        super(Addon.CATEGORY, "uwuifier", "Uwuifies your messages.");
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        String message = event.message;

        event.message = Uwuify.uwuify(message);
    }
}
