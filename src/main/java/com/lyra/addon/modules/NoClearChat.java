package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;


public class NoClearChat extends Module {
    public NoClearChat() {
        super(Addon.CATEGORY, "no-clear-chat", "Bypasses EssentialsX chat clear method.");
    }
    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        if (event.getMessage().getString().equals(" ")) {
            event.cancel();
        }
    }
}
