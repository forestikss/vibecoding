package ru.etc1337.client.modules.impl.misc;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.TextFormatting;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.input.EventInputKey;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.settings.impl.BindSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Click Friend", description = "Добавляет друга по кнопке", category = ModuleCategory.MISC)
public class ClickFriend extends Module {
    private final BindSetting key = new BindSetting("Кнопка добавления", this, -1);

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventInputKey eventInputKey) {
            if (eventInputKey.getKey() == key.getKey() && mc.pointedEntity instanceof LivingEntity) {
                String entityName = mc.pointedEntity.getName().getString();
                if (Client.getInstance().getFriendManager().isFriend(entityName)) {
                    Client.getInstance().getFriendManager().removeFriend(entityName);
                    Chat.send(entityName + TextFormatting.DARK_RED + " удален из друзей!");
                } else {
                    Client.getInstance().getFriendManager().addFriend(entityName);
                    Chat.send(entityName + TextFormatting.DARK_GREEN + " добавлен в друзья!");
                }
            }
        }
    }
}
