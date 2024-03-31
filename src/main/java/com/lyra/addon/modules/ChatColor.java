package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.ColorSetting;

import java.awt.*;
import java.util.Random;

public class ChatColor extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgExtra = settings.createGroup("Extra");

    private final Setting<Mode> styleMode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("color-mode")
        .description("Chat color style mode.")
        .defaultValue(Mode.Normal)
        .build()
    );
    private final Setting<Format> formatMode = sgGeneral.add(new EnumSetting.Builder<Format>()
        .name("format")
        .description("Different formats for chat color.")
        .defaultValue(Format.RRGGBB)
        .visible(() -> styleMode.get() == Mode.Gradient)
        .build()
    );
    private final Setting<SettingColor> startColor = sgGeneral.add(new ColorSetting.Builder()
        .name("start-color")
        .description("The Gradient start color.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> styleMode.get() == Mode.Gradient)
        .build()
    );
    private final Setting<SettingColor> endColor = sgGeneral.add(new ColorSetting.Builder()
        .name("end-color")
        .description("The Gradient end color.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> styleMode.get() == Mode.Gradient)
        .build()
    );
    private final Setting<SettingColor> fallbackColor = sgGeneral.add(new ColorSetting.Builder()
        .name("default-color")
        .description("A default color that gets used if the message is too big to send.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );

    private final Setting<Boolean> isBold = sgGeneral.add(new BoolSetting.Builder()
        .name("bold")
        .description("Enable Bold text style.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> isObfuscated = sgGeneral.add(new BoolSetting.Builder()
        .name("obfuscated")
        .description("Enable Obfuscated text style.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> isStrikethrough = sgGeneral.add(new BoolSetting.Builder()
        .name("strikethrough")
        .description("Enable Strikethrough text style.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> isUnderline = sgGeneral.add(new BoolSetting.Builder()
        .name("underline")
        .description("Enable Underline text style.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> isItalic = sgGeneral.add(new BoolSetting.Builder()
        .name("italic")
        .description("Enable Italic text style.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> isEmojiAllow = sgExtra.add(new BoolSetting.Builder()
        .name("emoji")
        .description("Allows you to use emojis with the : symbol.")
        .defaultValue(false)
        .build()
    );

    public ChatColor() {
        super(Addon.CATEGORY, "chat-color", "Better chat colors and style formatting.");
    }

    private String generateGradientText(String text) {
        int gradientSteps = text.length();
        String[] gradient = generateGradient(gradientSteps);

        StringBuilder gradientText = new StringBuilder();
        boolean isInsideEmoji = false;

        for (int i = 0; i < gradientSteps; i++) {
            char c = text.charAt(i);
            String addHex = (!isInsideEmoji ? "&" + gradient[i] : "");
            String addChar = "&" + getRandom();
            if(c == ' ') {
                gradientText.append(c);
            } else if (c == ':' && isEmojiAllow.get()) {
                if(!isInsideEmoji) {
                gradientText.append("&").append(getHex(fallbackColor.get().r, fallbackColor.get().g, fallbackColor.get().b));
                }
                gradientText.append(c);
                isInsideEmoji = !isInsideEmoji;
            } else if (isInsideEmoji && isEmojiAllow.get()) {
                gradientText.append(c);
            } else {
                gradientText.append(styleMode.get() == Mode.Rainbow ? addChar : "").append(styleMode.get() == Mode.Gradient ? addHex : "").append(textFormatting(String.valueOf(c), false));
            }
        }
        return gradientText.toString();
    }

    private String[] generateGradient(int steps) {

        String[] gradient = new String[steps];

        int redDiff = endColor.get().r - startColor.get().r;
        int greenDiff = endColor.get().g - startColor.get().g;
        int blueDiff = endColor.get().b - startColor.get().b;

        float redStep = (float) redDiff / (steps - 1);
        float greenStep = (float) greenDiff / (steps - 1);
        float blueStep = (float) blueDiff / (steps - 1);

        for (int i = 0; i < steps; i++) {
            int red = startColor.get().r + (int)(redStep * i);
            int green = startColor.get().g + (int)(greenStep * i);
            int blue = startColor.get().b + (int)(blueStep * i);

            gradient[i] = getHex(red, green, blue);
        }

        return gradient;
    }
    private String textFormatting(String text, boolean color) {
        return (color ? "&" + getHex(fallbackColor.get().r, fallbackColor.get().g, fallbackColor.get().b) : "")
            + (isBold.get() ? "&l" : "")
            + (isObfuscated.get() ? "&k" : "")
            + (isStrikethrough.get() ? "&m" : "")
            + (isUnderline.get() ? "&n" : "")
            + (isItalic.get() ? "&o" : "")
            + text;
    }
    private char getRandom() {
        char[] CHARACTERS = "0123456789abcdef".toCharArray();
        Random random = new Random();
        int randomIndex = random.nextInt(CHARACTERS.length);
        return CHARACTERS[randomIndex];
    }
    private String getHex(int r, int g, int b) {
        return String.format("#%06X", new Color(r, g, b).getRGB() & 0xFFFFFF);
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        String message = event.message;

        switch (styleMode.get()) {
            case Normal -> {
                message = textFormatting(event.message, true);
            }
            case Rainbow, Gradient ->  {
                message = generateGradientText(event.message);
            }
        }

        if (message.length() > 256) {
            message = event.message;
        }
        event.message = message;
    }
    public enum Mode {
        Normal,
        Rainbow,
        Gradient,
    }
    public enum Format {
        RRGGBB
    }

}
