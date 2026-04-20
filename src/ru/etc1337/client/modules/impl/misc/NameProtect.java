package ru.etc1337.client.modules.impl.misc;

import net.minecraft.util.text.TextFormatting;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Name Protect", description = "Скрывает никнеймы", category = ModuleCategory.MISC)
public class NameProtect extends Module {
    public BooleanSetting friends = new BooleanSetting("Скрывать друзей", this);
    public String patch(String text) {
        String out = text;
        if (isEnabled()) {
            out = text.replaceAll(mc.session.getUsername(), TextFormatting.RED + "stradix" + TextFormatting.RESET);
        }
        return out;
    }
}
