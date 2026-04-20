package ru.etc1337.client.modules.impl.misc;

import net.minecraft.util.math.MathHelper;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.input.EventScrolling;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Beautifully", description = "Улучшает игру", category = ModuleCategory.MISC)
public class Beautifully extends Module {
    public final MultiModeSetting mode = new MultiModeSetting("Тип", this, "Анимации в чате", "Улучшенный таб", "Улучшенный зум");
    public double zoom = 4.0D;
    static final double MIN_ZOOM = 4;
    static final double MAX_ZOOM = 400;

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventScrolling eventScrolling) {
            if (mode.get("Улучшенный зум").isEnabled()) {
                if (eventScrolling.getDelta() > 0 && zoom < MAX_ZOOM) {
                    zoom += eventScrolling.getDelta();
                } else if (eventScrolling.getDelta() < 0 && zoom > MIN_ZOOM) {
                    zoom += eventScrolling.getDelta();
                }
                zoom = MathHelper.clamp(zoom, MIN_ZOOM, MAX_ZOOM);
            }
        }
    }
}
