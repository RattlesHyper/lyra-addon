package com.lyra.addon.commands;

import com.lyra.addon.modules.SpectatorTeleport;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SptpCommand extends Command {
    public SptpCommand() {
        super("sptp", "Teleports you with Spectator Mode Teleport packet.");
    }
    SpectatorTeleport spectatorTeleport = Modules.get().get(SpectatorTeleport.class);
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("player", PlayerListEntryArgumentType.create()).executes(context -> {
                PlayerListEntry lookUpTarget = PlayerListEntryArgumentType.get(context);
                UUID uuid = lookUpTarget.getProfile().getId();
                sendTeleportPacket(uuid);
            return SINGLE_SUCCESS;
        }));
    }

    private void sendTeleportPacket(UUID uuid) {
        String beforeTp = spectatorTeleport.beforeTp.get();
        String afterTp = spectatorTeleport.afterTp.get();
        Integer delayTime = spectatorTeleport.delayTime.get();
        Boolean isDetectSpec = spectatorTeleport.isDetectSpec.get();
        Boolean enableBefore = spectatorTeleport.enableBefore.get();
        Boolean enableAfter = spectatorTeleport.enableAfter.get();

        if (mc.player.isSpectator() && isDetectSpec) {
            mc.player.networkHandler.sendPacket(new SpectatorTeleportC2SPacket(uuid));
        } else {
            if (enableBefore) {
                ChatUtils.sendPlayerMsg(beforeTp);
            }
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.schedule(() -> {
                mc.player.networkHandler.sendPacket(new SpectatorTeleportC2SPacket(uuid));
                if (enableAfter) {
                    ChatUtils.sendPlayerMsg(afterTp);
                }
                executor.shutdown();
            }, delayTime, TimeUnit.MILLISECONDS);

        }
    }

}
