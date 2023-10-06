package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
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
            playerName = null;
        })
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
        .defaultValue(List.of(
            "God, I love you so much %player%~",
            "%player% I'm already dripping.",
            "I need to feel you against me %player%~",
            "%player% I want your mouth on me~",
            "Oh god, you're so big %player%!",
            "Treat me like a whore!",
            "I want to see you play with yourself %player%~",
            "I want you to undress me~",
            "I want to taste you %player%~~",
            "Come for me %player%~",
            "Choke me!~",
            "Just like that %player%~! AUGHH~",
            "AHHH~ Right there %player%~",
            "Moan for me %player%~",
            "You like that %player%~?",
            "Play with me %player%~",
            "%player% I love the way you taste~",
            "I love your body %player%~~",
            "I love it when you touch me there~",
            "I love the way you moan %player%~",
            "I love how hard you can make me cum",
            "%player% Your tongue is magical.",
            "%player% You are not allowed to cum until I say so.",
            "I’m going to drain you %player%~!",
            "I know I am naughty~",
            "I want to f*ck you so hard right now %player%~!",
            "I am crazy for you!",
            "I wanna feel you. Taste you. Touch you %player%"
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
    String playerName;
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
        if (!PlayerUtils.isWithin(entity, 10)) return false;
        if (!PlayerUtils.canSeeEntity(entity) && !PlayerUtils.isWithin(entity, 10)) return false;
        if (!Pattern.matches(regex, entity.getEntityName())) return false;
        return entity.isPlayer();
    }

    //middle click mode
    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if(targetMode.get() == Mode.MiddleClick){
            if (event.action == KeyAction.Press && event.button == GLFW_MOUSE_BUTTON_MIDDLE && mc.currentScreen == null && mc.targetedEntity != null && mc.targetedEntity instanceof PlayerEntity) {
                playerName = mc.targetedEntity.getEntityName();
                target = mc.targetedEntity;

                if (message.get()) {
                    startMsg();
                }
            }
        }
    }
    @EventHandler
    private void onTick(TickEvent.Post event) {
        if(randomCum.get() && shouldCum()) {
            ItemStack milk = new ItemStack(Items.MILK_BUCKET);
            milk.setCustomName(Text.of("§4§l" +mc.player.getEntityName() + "'s §f§lCUM"));
            for (int i = 9; i < 11; i++) {
                mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(i, milk));
            }
            for (int i = 9; i < 11; i++) {
                InvUtils.drop().slot(i);
            }
        }
        if (target == null) return;

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

                ChatUtils.sendPlayerMsg(text.replaceAll("%player%", playerName));
                timer = delay.get();
            }
            else {
                timer--;
            }
        }
    }
    double addition = 0.0;
    @EventHandler
    private void onRender(Render3DEvent event) {
        if (target == null || !isRender.get()) return;

        Vec3d last = null;
        if (addition > 360) addition = 0;
        for (int i = 0; i < 360; i ++) {
            Random rand = new Random();
            double randomValue = 0.2 + (0.0 - 0.1) * rand.nextDouble();
            Color c1 = new Color(255, 0, 255, 255);;

            Vec3d tp = target.getPos();

            double rad = Math.toRadians(i);
            double sin = Math.sin(rad);
            double cos = Math.cos(rad);
            Vec3d c = new Vec3d(tp.x + sin, tp.y + randomValue, tp.z + cos);
            if (last != null) event.renderer.line(last.x, last.y, last.z, c.x, c.y, c.z, c1);
            last = c;
        }
    }

    @Override
    public void onDeactivate() {
        target = null;
        playerName = null;
        mc.player.getAbilities().flying = false;
    }
    public void setTarget() {
        TargetUtils.getList(targets, this::entityCheck, SortPriority.LowestDistance, 1);
        if(targets.isEmpty()) return;
        target = targets.get(0);
        playerName = target.getEntityName();
        startMsg();
    }
    public void startMsg() {
        if (message.get()) {
            ChatUtils.sendPlayerMsg("Come here " + playerName + " I want you uwu");
        }
    }
    public static boolean shouldCum() {
        double chance = Math.random();
        return chance <= 0.1;
    }
    public enum Mode {
        MiddleClick,
        Automatic
    }
    public enum Style {
        GulpGulp,
        Doggy
    }
}
