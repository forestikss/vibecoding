package ru.etc1337.api.notifications;

import ru.etc1337.Client;
import ru.etc1337.api.game.Sound;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.client.modules.impl.misc.ClientSounds;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager implements QuickImports {
    public final List<Notification> notifications = new ArrayList<>();

    public void publicity(String title, String content, int seconds, Notification.Type type, Notification.SoundType soundType) {
        if (mc.player != null && mc.world != null) {
            notifications.add(new Notification(title, content, type, seconds * 1000));
            if (!soundType.equals(Notification.SoundType.NONE)) {
                ClientSounds clientSounds = Client.getInstance().getModuleManager().get(ClientSounds.class);
                if (clientSounds.isEnabled()) {
                    switch (soundType) {
                        case MODULE_DISABLE, MODULE_ENABLE -> {
                            if (clientSounds.stateSounds.is("Нет")) return;
                            Sound.playSound(clientSounds.stateSounds.getCurrentMode() + ".wav",
                                    clientSounds.volume.getValue() / clientSounds.volume.getMax(),
                                    soundType.equals(Notification.SoundType.MODULE_DISABLE) ? 0.95F : 1F);
                        }
                        case NOTIFY -> {
                            if (clientSounds.notify.is("Нет")) return;
                            Sound.playSound(clientSounds.notify.getCurrentMode() + ".wav", clientSounds.volume.getValue() / clientSounds.volume.getMax(), 1.0F);
                        }
                    }
                }
            }
        }
    }
}