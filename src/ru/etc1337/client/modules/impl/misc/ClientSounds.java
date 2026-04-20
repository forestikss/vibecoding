package ru.etc1337.client.modules.impl.misc;

import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Client Sounds", description = "Добавляет звуки клиента", category = ModuleCategory.MISC)
public class ClientSounds extends Module {
    public final ModeSetting stateSounds = new ModeSetting("Модуль", this, "Нет", "Antic", "Cheers", "Chord", "Droplet", "Handoff", "Milestone", "Note", "Rebound");
    public final ModeSetting notify = new ModeSetting("Уведомление", this, "Нет", "Antic", "Cheers", "Chord", "Droplet", "Handoff", "Milestone", "Note", "Rebound");
    public final SliderSetting volume = new SliderSetting("Громкость", this, 50.0F, 1.0F, 100.0F, 0.5F);
}
