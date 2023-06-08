package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import java.awt.Color;

public class ChatColor extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgExtra = settings.createGroup("Extra");
    private final Setting < SettingColor > startColor = sgGeneral.add(new ColorSetting.Builder()
        .name("start-color")
        .description("The Gradient start color.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting < SettingColor > endColor = sgGeneral.add(new ColorSetting.Builder()
        .name("end-color")
        .description("The Gradient end color.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting < SettingColor > fallbackColor = sgGeneral.add(new ColorSetting.Builder()
        .name("default-color")
        .description("A default color that gets used if the message is too big to send.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting < Boolean > isFallback = sgGeneral.add(new BoolSetting.Builder()
        .name("enable-default")
        .description("Enable default color that only gets used if the message is too big to send or gradient is disabled.")
        .defaultValue(false)
        .build()
    );
    private final Setting < Boolean > isGradient = sgGeneral.add(new BoolSetting.Builder()
        .name("gradient")
        .description("Enable Gradient text style.")
        .defaultValue(false)
        .build()
    );
    private final Setting < Boolean > isBold = sgGeneral.add(new BoolSetting.Builder()
        .name("bold")
        .description("Enable Bold text style.")
        .defaultValue(false)
        .build()
    );
    private final Setting < Boolean > isObfuscated = sgGeneral.add(new BoolSetting.Builder()
        .name("obfuscated")
        .description("Enable Obfuscated text style.")
        .defaultValue(false)
        .build()
    );
    private final Setting < Boolean > isStrikethrough = sgGeneral.add(new BoolSetting.Builder()
        .name("strikethrough")
        .description("Enable Strikethrough text style.")
        .defaultValue(false)
        .build()
    );
    private final Setting < Boolean > isUnderline = sgGeneral.add(new BoolSetting.Builder()
        .name("underline")
        .description("Enable Underline text style.")
        .defaultValue(false)
        .build()
    );
    private final Setting < Boolean > isItalic = sgGeneral.add(new BoolSetting.Builder()
        .name("italic")
        .description("Enable Italic text style.")
        .defaultValue(false)
        .build()
    );
    private final Setting < Boolean > isEmojiAllow = sgExtra.add(new BoolSetting.Builder()
        .name("emoji")
        .description("Allows you to use emojis with the : symbol.")
        .defaultValue(false)
        .build()
    );

    public ChatColor() {
        super(Addon.CATEGORY, "chat-color", "Better chat colors and style formatting.");
    }

    private String generateGradientText(String text, String startColor, String endColor) {
        int gradientSteps = text.length();
        Color[] gradient = generateGradient(startColor, endColor, gradientSteps);

        StringBuilder gradientText = new StringBuilder();
        boolean isInsideEmoji = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Color color = gradient[i];
            String hexColor = String.format("#%06X", color.getRGB() & 0xFFFFFF);

            if (c == ':' && !isInsideEmoji && isEmojiAllow.get()) {
                gradientText.append(isGradient.get() ? "&" + hexColor : "").append(isBold.get() ? "&l" : "").append(isObfuscated.get() ? "&k" : "").append(isStrikethrough.get() ? "&m" : "").append(isUnderline.get() ? "&n" : "").append(isItalic.get() ? "&o" : "").append(c);
                isInsideEmoji = true;
            } else if (isInsideEmoji && c == ':' && isEmojiAllow.get()) {
                gradientText.append(c);
                isInsideEmoji = false;
            } else if (isInsideEmoji || isEmoji(c) || c == ' ') {
                gradientText.append(c);
            }
            else {
                gradientText.append(isGradient.get() ? "&" + hexColor : "").append(isBold.get() ? "&l" : "").append(isObfuscated.get() ? "&k" : "").append(isStrikethrough.get() ? "&m" : "").append(isUnderline.get() ? "&n" : "").append(isItalic.get() ? "&o" : "").append(c);
            }
        }

        return gradientText.toString();
    }

    private boolean isEmoji(char c) {
        int type = Character.getType(c);
        return type == Character.SURROGATE || type == Character.OTHER_SYMBOL;
    }

    private Color[] generateGradient(String startColor, String endColor, int steps) {
        Color start = Color.decode(startColor);
        Color end = Color.decode(endColor);

        Color[] gradient = new Color[steps];

        int redDiff = end.getRed() - start.getRed();
        int greenDiff = end.getGreen() - start.getGreen();
        int blueDiff = end.getBlue() - start.getBlue();

        float redStep = (float) redDiff / (steps - 1);
        float greenStep = (float) greenDiff / (steps - 1);
        float blueStep = (float) blueDiff / (steps - 1);

        for (int i = 0; i < steps; i++) {
            int red = start.getRed() + (int)(redStep * i);
            int green = start.getGreen() + (int)(greenStep * i);
            int blue = start.getBlue() + (int)(blueStep * i);

            gradient[i] = new Color(red, green, blue);
        }

        return gradient;
    }
    private String getColor(String red, String green, String blue) {
        return String.format("#%06X", (0xFFFFFF & new Color(Integer.parseInt(red), Integer.parseInt(green), Integer.parseInt(blue)).getRGB()));
    }
    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        String message = event.message;
        StringBuilder defaultText = new StringBuilder();
        String[] color = startColor.toString().split(" "), color2 = endColor.   toString().split(" "), color3 = fallbackColor.toString().split(" ");
        if (isGradient.get()) {
            message = generateGradientText(message, getColor(color[0], color[1], color[2]), getColor(color2[0], color2[1], color2[2]));
        }
        if (!isGradient.get() || message.length() > 256) {
            defaultText.append(isFallback.get() ? "&" + getColor(color3[0], color3[1], color3[2]) : "").append(isBold.get() ? "&l" : "").append(isObfuscated.get() ? "&k" : "").append(isStrikethrough.get() ? "&m" : "").append(isUnderline.get() ? "&n" : "").append(isItalic.get() ? "&o" : "").append(event.message);
            message = defaultText.toString();
        }
        if (defaultText.length() > 256) {
            message = event.message;
        }

        event.message = message;
    }
}
