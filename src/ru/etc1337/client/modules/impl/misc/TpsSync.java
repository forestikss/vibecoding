package ru.etc1337.client.modules.impl.misc;

import net.minecraft.util.math.MathHelper;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.input.EventScrolling;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Tps Sync", description = "Синхронизирует действия игрока с тиками сервера", category = ModuleCategory.MISC)
public class TpsSync extends Module {
    public final MultiModeSetting mode = new MultiModeSetting("Тип", this, "Удары");

    public double getTps() {
        return this.isEnabled() ? Client.getInstance().getTpsHandler().getTPS() : 20.0D;
    }
}
