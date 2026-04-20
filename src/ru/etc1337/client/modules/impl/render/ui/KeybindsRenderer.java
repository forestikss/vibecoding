package ru.etc1337.client.modules.impl.render.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.InputMappings;
import org.lwjgl.glfw.GLFW;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.animations.simple.Animation;
import ru.etc1337.api.animations.simple.Easing;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.render.EventRender2D;

import java.util.ArrayList;
import java.util.List;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.fonts.impl.TextBatcher;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.impl.render.ui.api.ElementInfo;
import ru.etc1337.client.modules.impl.render.ui.api.Header;
import ru.etc1337.client.modules.impl.render.ui.api.UIElement;

import java.util.ArrayList;
import java.util.List;

@ElementInfo(name = "Keybinds", icon = "p", initX = 8.0F, initY = 28.0F, initHeight = 17.0F)
public class KeybindsRenderer extends UIElement {
    private final BooleanSetting removeHeader = new BooleanSetting("Убрать заголовок", this);
    private final BooleanSetting fullSize = new BooleanSetting("Полный размер", this);

    private final BooleanSetting visibleOnly = new BooleanSetting("Скрывать если пусто", this).setVisible(() -> !removeHeader.isEnabled());
    private final TextBatcher textBatcher = new TextBatcher(Fonts.SEMIBOLD_14);
    private final Animation animation = new Animation(Easing.SINE_IN_OUT, 150);
    private List<Storage> cachedModules = new ArrayList<>();
    private long lastCacheUpdate = 0;
    {
        animation.update(1f);
        for (int i = 0; i < 20; i++) animation.update(1f);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRender2D eventRender2D) {
            MatrixStack matrixStack = eventRender2D.getMatrixStack();
            float x = getDraggable().getX();
            float y = getDraggable().getY();

            // Обновляем кеш раз в 100мс чтобы не мигало
            long now = System.currentTimeMillis();
            if (now - lastCacheUpdate > 100) {
                cachedModules = getModules();
                lastCacheUpdate = now;
            }
            List<Storage> modules = cachedModules;

            animation.update(!(mc.currentScreen instanceof ChatScreen) && visibleOnly.isEnabled() && modules.isEmpty() ? 0 : 1);
            float value = animation.getValue();
            if (value < 0.05F) return;

            if (!removeHeader.isEnabled()) {
                Header.drawModernHeader(matrixStack, getDraggable(), x, y, getIcon(), "Keybinds", 1, value * 255);
            }

            if (modules.isEmpty()) return;
            float hotkeyOffsetY = y;
            hotkeyOffsetY += 19.5f; // 3 - offset
            boolean isIgnore = fullSize.isEnabled();
            float scale = isIgnore ? 1 : 0.8f;
            float offset = isIgnore ? 18 : 15;
            for (Storage module : modules) {
                String key = InputMappings.getInputByCode(module.getKey(), GLFW.GLFW_KEY_UNKNOWN)
                        .getTranslationKey().replace("key.keyboard.", "").toUpperCase();
                Header.drawModernHeader(matrixStack, getDraggable(), x, hotkeyOffsetY, module.getIcon(), module.getName(), scale, value * 255, key);

                hotkeyOffsetY += offset;
            }
        }
    }

    private List<Storage> getModules() {
        List<Storage> storageList = new ArrayList<>();
        for (Module module : Client.getInstance().getModuleManager().getModules()) {
            if (module.getKey() != -1 && module.isEnabled()) {
                storageList.add(new Storage(module.getName(), module.getKey(), module.getModuleInfo().category().getIcon()));
            }
            module.getSettings().forEach((setting) -> {
                if (setting instanceof BooleanSetting booleanSetting && setting.getKey() != -1 && booleanSetting.isEnabled()) {
                    storageList.add(new Storage(booleanSetting.getName(), booleanSetting.getKey(), module.getModuleInfo().category().getIcon()));
                }
                if (setting instanceof MultiModeSetting multiModeSetting) {
                    multiModeSetting.getBoolSettings().forEach(boolSetting -> {
                        if (boolSetting.getKey() != -1 && boolSetting.isEnabled()) {
                            storageList.add(new Storage(boolSetting.getName(), boolSetting.getKey(), module.getModuleInfo().category().getIcon()));
                        }
                    });
                }
                if (setting instanceof ModeSetting modeSetting) {
                    for (String mode : modeSetting.getModes()) {
                        int key = modeSetting.getModeKey(mode);
                        String selectedMode = modeSetting.getModeString(key);
                        if (key != -1 && selectedMode.equals(modeSetting.getCurrentMode())) {
                            storageList.add(new Storage(modeSetting.getModeString(key), key, module.getModuleInfo().category().getIcon()));
                        }
                    }
                }
            });
        }
        return storageList;
    }

    @AllArgsConstructor @Getter @Setter
    static class Storage {
        private String name;
        private int key;
        private String icon;
    }
}