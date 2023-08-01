package com.lyra.addon;

import com.lyra.addon.commands.*;
import com.lyra.addon.modules.*;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;


public class Addon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Lyra Addon");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Lyra Addon for Meteor Client");

        // Modules
        Modules.get().add(new ItemDropper());
        Modules.get().add(new RainbowArmor());
        Modules.get().add(new CommandAura());
        Modules.get().add(new ChatColor());
        Modules.get().add(new PacketScaffold());
        Modules.get().add(new AutoMap());
        Modules.get().add(new ForEach());
        Modules.get().add(new SpectatorTeleport());
        Modules.get().add(new DisableTooltips());
        Modules.get().add(new AutoClip());
        Modules.get().add(new NoClearChat());
        Modules.get().add(new TpAura());
        LOG.info("Added modules");

        // Commands
        Commands.add(new StackCommand());
        Commands.add(new StackCommand());
        Commands.add(new ItemStealer());
        Commands.add(new RenameCommand());
        Commands.add(new SptpCommand());
        Commands.add(new SummonCommand());
        LOG.info("Added commands");
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.lyra.addon";
    }
}
