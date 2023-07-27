package com.lyra.addon.modules;

import com.lyra.addon.Addon;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;

public class DisableTooltips extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<String> tooltipText = sgGeneral.add(new StringSetting.Builder()
        .name("text")
        .description("Text to replace tooltips with.")
        .defaultValue("name")
        .build()
    );

    public DisableTooltips() {
        super(Addon.CATEGORY, "disable-tooltips", "Hides tooltips with custom name.");
    }
    public Setting<String> getTooltipText() {
        return this.tooltipText;
    }
}
