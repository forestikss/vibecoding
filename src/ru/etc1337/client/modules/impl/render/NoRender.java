package ru.etc1337.client.modules.impl.render;

import net.minecraft.potion.Effects;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "No Render", description = "Убирает лишние компоненты с экрана",category = ModuleCategory.RENDER)
public class NoRender extends Module {
    public final MultiModeSetting remove = new MultiModeSetting("Убрать", this,
            "Тряска при ударе", "Интерполяцию руки", "Огонь",  "Анимацию тотема", "Таблицу", "Линию босса", "Частицы", "Тайтлы",
            "Плохие эффекты", "Ограничение просмотра");

}
