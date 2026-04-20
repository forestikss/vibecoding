package ru.etc1337.client.modules.impl.misc;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.VoiceChatScreen;
import de.maxhenkel.voicechat.gui.onboarding.OnboardingManager;
import de.maxhenkel.voicechat.voice.client.*;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TranslationTextComponent;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.input.EventInputKey;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.settings.impl.BindSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Voice Chat", description = "Голосовой чат между игроками", category = ModuleCategory.MISC)
public class VoiceChat extends Module {
    public final BindSetting keyVoice = new BindSetting("Говорить", this, -1);
    public final BindSetting keyOpenVoice = new BindSetting("Меню", this, -1);
    public final BindSetting keyDisable = new BindSetting("Отключить", this, -1);


    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            KeyEvents.KEY_PTT = new KeyBinding("key.push_to_talk", keyVoice.getKey(), "key.categories.voicechat");
            KeyEvents.KEY_VOICE_CHAT = new KeyBinding("key.voice_chat", keyOpenVoice.getKey(), "key.categories.voicechat");
            KeyEvents.KEY_DISABLE = new KeyBinding("key.disable_voice_chat", keyDisable.getKey(), "key.categories.voicechat");
        }
        if (event instanceof EventInputKey e) {
            if (e.getKey() == keyVoice.getKey()) {
                PTTKeyHandler.pttKeyDown = !e.isReleased();
            }

            if (e.isReleased()) return;
            ClientPlayerEntity player = mc.player;
            if (player == null) {
                return;
            }

            ClientVoicechat client = ClientManager.getClient();
            ClientPlayerStateManager playerStateManager = ClientManager.getPlayerStateManager();

            if (keyOpenVoice.getKey() == e.getKey()) {
                if (Screen.hasAltDown()) {
                    if (Screen.hasControlDown()) {
                        VoicechatClient.CLIENT_CONFIG.onboardingFinished.set(false).save();
                        player.sendStatusMessage(new TranslationTextComponent("message.voicechat.onboarding.reset"), true);
                    } else {
                        ClientManager.getDebugOverlay().toggle();
                    }
                } else {
                    mc.displayGuiScreen(new VoiceChatScreen());
                }
            }

            if (keyVoice.getKey() == e.getKey()) {
                checkConnected();
            }
            if (keyDisable.getKey() == e.getKey()) {
                playerStateManager.setDisabled(!playerStateManager.isDisabled());
            }
        }
    }


    private boolean checkConnected() {
        if (ClientManager.getClient() == null || ClientManager.getClient().getConnection() == null || !ClientManager.getClient().getConnection().isInitialized()) {
            sendNotConnectedMessage();
            return false;
        }
        return true;
    }

    private void sendNotConnectedMessage() {
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            Voicechat.LOGGER.warn("Voice chat not connected");
            return;
        }
        player.sendStatusMessage(new TranslationTextComponent("message.voicechat.voice_chat_not_connected"), true);
    }

    @Override
    public void onEnable() {
        if (OnboardingManager.isOnboarding()) {
            OnboardingManager.startOnboarding(null);
            Chat.sendError("Для работы VoiceChat необходимо пройти обучение!");
            this.toggle();
        }
        super.onEnable();
    }
}