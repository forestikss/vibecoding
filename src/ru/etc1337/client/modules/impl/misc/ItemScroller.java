package ru.etc1337.client.modules.impl.misc;

import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Item Scroller", description = "Убирает задержку перемещения преметов", category = ModuleCategory.MISC)
public class ItemScroller extends Module {
    public final SliderSetting delay = new SliderSetting("Задержка", this, 50, 0, 200, 1);
}
