package ru.etc1337.client.modules.impl.misc;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.TextFormatting;
import ru.etc1337.Client;
import ru.etc1337.api.bot.BotConnection;
import ru.etc1337.api.bot.BotManager;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.packet.EventReceivePacket;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Bots", description = "Система ботов-твинков", category = ModuleCategory.MISC)
public class Bots extends Module {

    public final BooleanSetting showChat = new BooleanSetting("Видеть чат ботов", this);
    public final BooleanSetting spoofRP  = new BooleanSetting("Спуфать ресурспак", this);

    private final BotManager botManager = new BotManager();

    public BotManager getBotManager() { return botManager; }

    @Override
    public void onDisable() {
        botManager.disconnectAll();
        super.onDisable();
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            // Синхронизируем настройки
            for (BotConnection bot : botManager.getAll()) {
                bot.setShowChat(showChat.isEnabled());
                bot.setSpoofResourcePack(spoofRP.isEnabled());
            }
            botManager.tick();
        }

        // Перехватываем пакеты для ботов
        if (event instanceof EventReceivePacket e) {
            for (BotConnection bot : botManager.getAll()) {
                if (bot.getNetworkManager() != null &&
                    bot.getNetworkManager().getNetHandler() != null) {
                    // Пакеты бота обрабатываются в его собственном NetworkManager
                    // Здесь обрабатываем только если пакет пришёл от бота
                }
            }
        }

        if (event instanceof EventRender2D e) {
            renderBotChat(e.getMatrixStack());
        }
    }

    private void renderBotChat(MatrixStack ms) {
        for (BotConnection bot : botManager.getAll()) {
            // Системные сообщения — всегда
            String msg;
            while ((msg = bot.getChatMessages().poll()) != null) {
                Chat.send(TextFormatting.GRAY + "[Bot:" + bot.getName() + "] " + TextFormatting.WHITE + msg);
            }
        }
    }
}
