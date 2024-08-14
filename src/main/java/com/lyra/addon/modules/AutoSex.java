package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import com.lyra.addon.utils.SetItem;
import com.mojang.authlib.GameProfile;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

public class AutoSex extends Module{

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPos = settings.createGroup("Sex Position");
    private final SettingGroup sgMessage = settings.createGroup("Message");
    private final SettingGroup sgRender = settings.createGroup("Render");


    // General
    private final Setting<Mode> targetMode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("target-mode")
        .description("The mode at which to follow the player.")
        .defaultValue(Mode.Automatic)
        .onChanged(onChanged -> {
            target = null;
        })
        .build()
    );
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum range to set target.")
        .defaultValue(6)
        .min(0)
        .sliderMax(6)
        .build()
    );
    private final Setting<Boolean> sexPos = sgGeneral.add(new BoolSetting.Builder()
        .name("sex-position")
        .description("Set a position to stick to player.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> randomCum = sgGeneral.add(new BoolSetting.Builder()
        .name("random-cum")
        .description("Randomly drops cum.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> message = sgGeneral.add(new BoolSetting.Builder()
        .name("message")
        .description("Sends dirty messages in chat.")
        .defaultValue(false)
        .build()
    );

    // Sex position
    private final Setting<Integer> sexDelay = sgPos.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay for sex movements in ticks")
        .defaultValue(3)
        .sliderRange(0, 20)
        .visible(sexPos::get)
        .build()
    );
    private final Setting<Style> sexStyle = sgPos.add(new EnumSetting.Builder<Style>()
        .name("style")
        .description("The style for sticking to player.")
        .defaultValue(Style.GulpGulp)
        .visible(sexPos::get)
        .build()
    );

    // Message
    private final Setting<Integer> delay = sgMessage.add(new IntSetting.Builder()
        .name("delay")
        .description("The delay between specified messages in ticks.")
        .defaultValue(80)
        .min(0)
        .sliderMax(200)
        .visible(message::get)
        .build()
    );

    private final Setting<Boolean> random = sgMessage.add(new BoolSetting.Builder()
        .name("randomize")
        .description("Selects a random message from your spam message list.")
        .defaultValue(true)
        .visible(message::get)
        .build()
    );

    private final Setting<List<String>> messages = sgMessage.add(new StringListSetting.Builder()
        .name("messages")
        .description("Messages to use for dirty talk.")
        .defaultValue(Arrays.asList(
            "I want you to make me your filthy slut~",
            "Fuck me so hard I can't walk straight~",
            "I want to taste every drop of you, %player%~",
            "Fill all my holes~",
            "Make me choke on you, %player%~",
            "Make me squirt all over you~",
            "Fuck me like the dirty slut I am, AUGHH~",
            "I want you all over my face, %player%~",
            "I want you to use me until I'm sore~",
            "Make me cum so hard I can't stop shaking, %player%~",
            "I want to be your cum slut~",
            "Fuck me until I'm a dripping mess, %player%~",
            "Make me scream your name while you fuck me~",
            "I want you to use me however you want, %player%~",
            "Fill my mouth and make me swallow, AHhhH~",
            "Make me cum all over you~",
            "Fuck me until I beg for mercy, %player%~",
            "I want to be your personal cum dump~",
            "Use me like your personal toy, %player%~",
            "I want you to ruin me, %player%~",
            "Make me cum all over your fingers, %player%~",
            "I crave your touch everywhere",
            "I need you to dominate me, mMMM!~"

        ))
        .visible(message::get)
        .build()
    );
    private final Setting<Boolean> isRender = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Render the target.")
        .defaultValue(true)
        .build()
    );

    public AutoSex() {
        super(Addon.CATEGORY, "auto-sex", "Automatic Minecraft Sex RP.");
    }
    private final List<Entity> targets = new ArrayList<>();
    String regex = "[A-Za-z0-9_]+";
    private int messageI, timer, timerSex, sexI;
    static double renderY;
    double addition = 0.0;
    Entity target = null;

    @Override
    public void onActivate() {
        if(targetMode.get() == Mode.Automatic){
            setTarget();
        }
    }

    private boolean entityCheck(Entity entity) {
        if (entity.equals(mc.player) || entity.equals(mc.cameraEntity)) return false;
        if ((entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) || !entity.isAlive()) return false;
        if (!PlayerUtils.isWithin(entity, range.get())) return false;
        if (!PlayerUtils.canSeeEntity(entity) && !PlayerUtils.isWithin(entity, range.get())) return false;
        if (Pattern.matches(regex, EntityUtils.getName(entity))) return true;
        return entity.isPlayer();
    }

    //middle click mode
    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if(targetMode.get() == Mode.MiddleClick) {
            if (event.action == KeyAction.Press && event.button == GLFW_MOUSE_BUTTON_MIDDLE && mc.currentScreen == null) {
                if (mc.targetedEntity instanceof PlayerEntity) {
                    target = mc.targetedEntity;

                    if (message.get()) {
                        startMsg();
                    }
                } else  {
                    target = null;
                    mc.player.getAbilities().flying = false;
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if(randomCum.get() && shouldCum()) {
            ItemStack milk = new ItemStack(Items.MILK_BUCKET);
            milk.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§4§l" + EntityUtils.getName(mc.player) + "'s §f§lCUM"));
            for (int i = 9; i < 11; i++) {
                SetItem.set(milk, i);
            }
            for (int i = 9; i < 11; i++) {
                InvUtils.drop().slot(i);
            }
        }

        if (target == null) return;

        checkEntity();

        if(sexPos.get()) {
            mc.player.getAbilities().flying = true;

            if (timerSex <= 0) {
                if(sexStyle.get() == Style.GulpGulp) {
                    Rotations.rotate(Rotations.getYaw(target), 45);
                    if(sexI == 0) {
                        Position head = target.raycast(0.2, 1f / 20f, false).getPos();
                        mc.player.setPosition(head.getX(), head.getY() - 0.5, head.getZ());
                        sexI = 1;
                    } else {
                        Position head = target.raycast(0.5, 1f / 20f, false).getPos();
                        mc.player.setPosition(head.getX(), head.getY() - 0.5, head.getZ());
                        sexI = 0;
                    }
                }
                if(sexStyle.get() == Style.Doggy) {
                    Rotations.rotate(Rotations.getYaw(target), 25);
                    if(sexI == 0) {
                        Position head = target.raycast(-0.2, 1f / 20f, false).getPos();
                        mc.player.setPosition(head.getX(), target.getY(), head.getZ());
                        sexI = 1;
                    } else {
                        Position head = target.raycast(-0.5, 1f / 20f, false).getPos();
                        mc.player.setPosition(head.getX(), target.getY(), head.getZ());
                        sexI = 0;
                    }
                }

                timerSex = sexDelay.get();
            }
            else {
                timerSex--;
            }
        }

        if (message.get()) {
            if (messages.get().isEmpty()) return;

            if (timer <= 0) {
                int i;
                if (random.get()) {
                    i = Utils.random(0, messages.get().size());
                }
                else {
                    if (messageI >= messages.get().size()) messageI = 0;
                    i = messageI++;
                }

                String text = messages.get().get(i);

                ChatUtils.sendPlayerMsg(text.replaceAll("%player%", EntityUtils.getName(target)));
                timer = delay.get();
            }
            else {
                timer--;
            }
        }
    }
    @EventHandler
    private void onRender(Render3DEvent event) {
        if (target == null || !isRender.get()) return;

        Vec3d last = null;
        if (addition > 360) addition = 0;
        for (int i = 0; i < 360; i ++) {
            Color c1 = new Color(255, 0, 255, 255);;
            Vec3d tp = target.getPos();

            double rad = Math.toRadians(i);
            double sin = Math.sin(rad);
            double cos = Math.cos(rad);
            Vec3d c = new Vec3d(tp.x + sin, tp.y + getRenderY(), tp.z + cos);
            if (last != null) event.renderer.line(last.x, last.y, last.z, c.x, c.y, c.z, c1);
            last = c;
        }
    }

    @Override
    public void onDeactivate() {
        target = null;
        mc.player.getAbilities().flying = false;
    }
    private void checkEntity() {
        List <String> playerNamesList = mc.player.networkHandler.getPlayerList().stream()
            .map(PlayerListEntry::getProfile)
            .map(GameProfile::getName)
            .toList();

        if (!playerNamesList.contains(EntityUtils.getName(target)) && targetMode.get() == Mode.Automatic) {
            target = null;
        }

        if (target == null && targetMode.get() == Mode.Automatic) {
            setTarget();
        }
    }
    private void setTarget() {
        TargetUtils.getList(targets, this::entityCheck, SortPriority.LowestDistance, 1);
        if(targets.isEmpty()) return;
        target = targets.get(0);
        startMsg();
    }
    private static double getRenderY() {
        Random rand = new Random();
        double randomValue = 0.2 + (0.0 - 0.05) * rand.nextDouble();
        if (renderY >= 0.3) {
            renderY = 0;
        }
        renderY+= randomValue;
        return renderY;
    }
    private void startMsg() {
        if (message.get()) {
            ChatUtils.sendPlayerMsg("Come here " + EntityUtils.getName(target) + " I want you uwu");
        }
    }
    private static boolean shouldCum() {
        double chance = Math.random();
        return chance <= 0.1;
    }
    private enum Mode {
        MiddleClick,
        Automatic
    }
    private enum Style {
        GulpGulp,
        Doggy
    }
}
