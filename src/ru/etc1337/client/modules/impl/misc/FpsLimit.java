package ru.etc1337.client.modules.impl.misc;

import org.lwjgl.glfw.GLFW;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "FPS Limit", description = "Ограничивает FPS когда окно свёрнуто", category = ModuleCategory.MISC)
public class FpsLimit extends Module {

    private final SliderSetting bgFps = new SliderSetting("FPS в фоне", this, 10f, 1f, 60f, 1f);
    private int savedLimit = -1;

    @Override
    public void onDisable() {
        // восстанавливаем оригинальный лимит
        if (savedLimit != -1) {
            mc.getMainWindow().setFramerateLimit(savedLimit);
            savedLimit = -1;
        }
        super.onDisable();
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventUpdate)) return;

        boolean focused = GLFW.glfwGetWindowAttrib(window.getHandle(), GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;

        if (!focused) {
            // сохраняем текущий лимит один раз
            if (savedLimit == -1) {
                savedLimit = mc.gameSettings.framerateLimit;
            }
            int limit = (int) bgFps.getValue();
            if (mc.gameSettings.framerateLimit != limit) {
                mc.getMainWindow().setFramerateLimit(limit);
                mc.gameSettings.framerateLimit = limit;
            }
        } else {
            // восстанавливаем
            if (savedLimit != -1) {
                mc.getMainWindow().setFramerateLimit(savedLimit);
                mc.gameSettings.framerateLimit = savedLimit;
                savedLimit = -1;
            }
        }
    }
}
