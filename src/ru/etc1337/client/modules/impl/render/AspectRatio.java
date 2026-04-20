package ru.etc1337.client.modules.impl.render;

import lombok.Getter;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@Getter
@ModuleInfo(name = "Aspect Ratio", description = "Растяг экрана", category = ModuleCategory.RENDER)
public class AspectRatio extends Module {
    public final SliderSetting aspect = new SliderSetting("Значение", this, 1.0f, 0.5f, 2.0f, 0.01f);
}