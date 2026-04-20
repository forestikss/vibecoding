package ru.etc1337.client.modules.impl.misc;

import net.minecraft.util.text.TextFormatting;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventTotemPop;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Totem Pop Counter", description = "Счетчик сноса тотемов", category = ModuleCategory.MISC)
public class TotemPopCounter extends Module {

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventTotemPop eventTotemPop) {
            if (eventTotemPop.getEntity() == mc.player) return;

            String s = TextFormatting.GREEN + eventTotemPop.getEntity().getName().getString() +
                    TextFormatting.WHITE + " попнул " + TextFormatting.AQUA +
                    (eventTotemPop.getPops() > 1 ? eventTotemPop.getPops() + "" + TextFormatting.WHITE + " тотемов!" : TextFormatting.WHITE + "тотем!");
            Chat.send(s);
        }
    }
}
