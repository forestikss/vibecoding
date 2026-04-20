package ru.etc1337.client.modules.impl.render;

import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Swing Animations", description = "Анимации рук", category = ModuleCategory.RENDER)
public class SwingAnimations extends Module {
    public final ModeSetting animationMode = new ModeSetting("Режим", this,
            "Smooth Translate",
            "Jump Translate",
            "Smooth Old",
            "Smooth",
            "Default",
            "Big",
            "Fap",
            "Snap", "Smooth Back", "Swingless");

    public final SliderSetting animationSmooth = new SliderSetting("Плавность анимации", this, 5.0f, 0.0f, 20.0f, 1.0f).setVisible(()
            -> !this.animationMode.getCurrentMode().equals("Без анимации") && !this.animationMode.is("Swingless"));
    public final SliderSetting animationStrength = new SliderSetting("Сила анимации", this,70.0f, 10.0f, 100.0f, 10.0f).setVisible(()
            -> !this.animationMode.getCurrentMode().equals("Без анимации") && !this.animationMode.is("Snap") && !this.animationMode.is("Swingless"));
    public final SliderSetting rotationAngle = new SliderSetting("Угол поворота", this,40.0f, 0.0f, 70.0f, 1.0f).setVisible(()
            -> this.animationMode.getCurrentMode().equals("Smooth Translate") && !this.animationMode.is("Snap") && !this.animationMode.is("Swingless"));

}
