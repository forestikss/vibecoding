package ru.etc1337.client.modules;

import lombok.Getter;
import lombok.Setter;
import ru.etc1337.Client;
import ru.etc1337.api.notifications.Notification;
import ru.etc1337.api.settings.api.Parent;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.handler.EventListener;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.settings.Setting;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.render.ui.NotificationRenderer;
import ru.etc1337.client.modules.impl.render.ui.ModuleNotifyRenderer;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Module extends Parent implements EventListener, QuickImports {
    private final ModuleInfo moduleInfo = this.getClass().getAnnotation(ModuleInfo.class);

    private final String name;
    private final String description;
    private final ModuleCategory category;

    @Setter
    private int key;
    private boolean enabled;

    public boolean isEnabled() { return enabled; }

    private final List<Setting> settings = new ArrayList<>();

    public Module() {
        this.name = getModuleInfo().name();
        this.description = getModuleInfo().description();
        this.category = getModuleInfo().category();
        this.key = getModuleInfo().key();
    }

    public Module(String name, String description, ModuleCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.key = -1;
    }

    public void onEnable() {
        Client.getInstance().getNotificationManager().publicity(this.getName(), "Was Enabled!", 2,
                Notification.Type.CUSTOM, Notification.SoundType.MODULE_ENABLE);
        Client.getEventManager().register(this);
    }

    public void onDisable() {
        Client.getInstance().getNotificationManager().publicity(this.getName(), "Was Disabled", 2,
                Notification.Type.CUSTOM, Notification.SoundType.MODULE_DISABLE);
        Client.getEventManager().unregister(this);
    }

    public void toggle() {
        this.enabled = !this.enabled;
        NotificationRenderer.push(this.getName(), this.enabled);
        ModuleNotifyRenderer.push(this.getName(), this.enabled);
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
        this.enabled = enabled;
    }

    @Override
    public void onEvent(Event event) { }
}
