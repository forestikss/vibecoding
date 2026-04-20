package ru.etc1337.client.modules.impl.render;

import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Custom Models", description = "Кастомные модели игрока", category = ModuleCategory.RENDER)
public class CustomModels extends Module {
    public ModeSetting mode = new ModeSetting("Выбор модели", this, "Rabbit", "Freddy Bear", "Demon");
    public BooleanSetting friends = new BooleanSetting("На друзей", this);
}
