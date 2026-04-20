package ru.etc1337.client.modules.impl.render;

import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Chunk Animator", description = "Анимирует появление чанков", category = ModuleCategory.RENDER)
public class ChunkAnimator extends Module {
    public SliderSetting time = new SliderSetting("Time", this, 800, 50, 1500, 1);
    public ModeSetting modes = new ModeSetting("Modes",this, "Linear", "Quad", "Cube", "Quarta", "Expo");
}
