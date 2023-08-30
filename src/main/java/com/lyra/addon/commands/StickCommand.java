package com.lyra.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import com.lyra.addon.utils.Timer;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class StickCommand extends Command {
    private final static SimpleCommandExceptionType CANT_STICK_TO_SELF = new SimpleCommandExceptionType(Text.literal("You can not stick to yourself."));
    private final StaticListener shiftListener = new StaticListener();
    private PlayerEntity target;
    public StickCommand() {
        super("stick", "Sticks you to target player.");
    }
    static boolean keepStuck = false;
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("reset").executes(context -> {
            keepStuck = false;
            return SINGLE_SUCCESS;
        }));

        builder.then(argument("player", PlayerArgumentType.create()).executes(context -> {
            target = PlayerArgumentType.get(context);
            if (Objects.equals(target.getEntityName(), mc.player.getEntityName())) throw CANT_STICK_TO_SELF.create();
            keepStuck = true;
            new Thread(() -> {
                for (int i = 1; i > 0; i++) {
                    if (mc.player != null && target != null && mc.player.distanceTo(target) < 6 && keepStuck)
                        mc.player.setPosition(target.getX(), target.getY() + 2, target.getZ());
                }
			}).start();
            mc.player.sendMessage(Text.literal("Sneak to un-stick."), true);
            MeteorClient.EVENT_BUS.subscribe(shiftListener);
            return SINGLE_SUCCESS;
        }));
    }
    private static class StaticListener {
        @EventHandler
        private void onKey(KeyEvent event) {
            if (mc.options.sneakKey.matchesKey(event.key, 0) || mc.options.sneakKey.matchesMouse(event.key)) {
                if (Modules.get().isActive(Freecam.class)) return;
                keepStuck = false;
                event.cancel();
                MeteorClient.EVENT_BUS.unsubscribe(this);
            }
        }
    }
}
