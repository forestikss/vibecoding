package ru.etc1337.client.modules.impl.render;

import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Gamma", description = "Делает мир ярче", category = ModuleCategory.RENDER)
public class Gamma extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", this, "Гамма", "Зелье");
    private final SliderSetting bright = new SliderSetting("Яркость", this, 2, 1, 10, 0.1F).setVisible(() -> mode.is("Гамма"));

    @Override
    public void onEvent(Event e) {
        if (e instanceof EventUpdate eventUpdate) {
            if (mc.player == null || mc.gameSettings == null) return;
            if (mode.is("Гамма")) {
                mc.gameSettings.gamma = bright.getValue();
            } else {
                mc.gameSettings.gamma = 0;
            }
            if (mode.is("Зелье")) {
                mc.player.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, 999999999, 1));
            } else {
                mc.player.removePotionEffect(Effects.NIGHT_VISION);
            }
        }
    }


    @Override
    public void onDisable() {
        if (mc.player == null || mc.gameSettings == null) return;
        mc.gameSettings.gamma = 0;
        mc.player.removeActivePotionEffect(new EffectInstance(Effects.NIGHT_VISION).getPotion());
        super.onDisable();
    }
}
