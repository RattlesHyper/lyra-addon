package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;

public class SpectatorTeleport extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgCommands = settings.createGroup("Commands");

    public final Setting < Boolean > isDetectSpec = sgGeneral.add(new BoolSetting.Builder()
        .name("detect-gamemode")
        .description("Detects if you're in spectator mode and sends the packet directly.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> enableBefore = sgCommands.add(new BoolSetting.Builder()
        .name("enable-before")
        .description("Enables the command to send before sending spectator teleport packet.")
        .defaultValue(true)
        .build()
    );
    public final Setting<String> beforeTp = sgCommands.add(new StringSetting.Builder()
        .name("before-tp")
        .visible(enableBefore::get)
        .description("The specified message sent to the server before sending Spectator Teleport packet.")
        .defaultValue("/gamemode spectator")
        .build()
    );
    public final Setting<Integer> delayTime = sgCommands.add(new IntSetting.Builder()
        .name("delay-in-ms")
        .description("Delay after sending the command.")
        .defaultValue(100)
        .sliderRange(1, 500)
        .build()
    );
    public final Setting<Boolean> enableAfter = sgCommands.add(new BoolSetting.Builder()
        .name("enable-after")
        .description("Enables the command to send after sending spectator teleport packet.")
        .defaultValue(true)
        .build()
    );
    public final Setting<String> afterTp = sgCommands.add(new StringSetting.Builder()
        .name("after-tp")
        .visible(enableAfter::get)
        .description("The specified message sent to the server after sending Spectator Teleport packet.")
        .defaultValue("/gamemode creative")
        .build()
    );
    public SpectatorTeleport() {
        super(Addon.CATEGORY, "spectator-teleport", "Settings for " + Config.get().prefix.get() + "sptp command. Remove the messages if you don't need them.");
    }
    public void onActivate() {
        error("This is not a module. this is only for " + Config.get().prefix.get() +"sptp command config.");
        this.toggle();
    }
}
