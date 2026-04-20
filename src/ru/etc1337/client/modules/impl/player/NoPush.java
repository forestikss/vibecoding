package ru.etc1337.client.modules.impl.player;

import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "No Push", description = "Убирает отталкивания", category = ModuleCategory.PLAYER)
public class NoPush extends Module {
    public final MultiModeSetting mode = new MultiModeSetting("Выбор", this, "Игроки", "Блоки", "Вода");
}
