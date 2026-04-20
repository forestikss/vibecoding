package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.audiodevice.SelectMicrophoneScreen;
import de.maxhenkel.voicechat.gui.audiodevice.SelectSpeakerScreen;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumesScreen;
import de.maxhenkel.voicechat.gui.widgets.*;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import de.maxhenkel.voicechat.voice.client.speaker.AudioType;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.shaders.impl.Round;


import javax.annotation.Nullable;

public class VoiceChatSettingsScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/gui_voicechat_settings.png");
    private static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.voice_chat_settings.title");

    private static final ITextComponent ASSIGN_TOOLTIP = new TranslationTextComponent("message.voicechat.press_to_reassign_key");
    private static final ITextComponent PUSH_TO_TALK = new TranslationTextComponent("message.voicechat.activation_type.ptt");
    private static final ITextComponent ADJUST_VOLUMES = new TranslationTextComponent("message.voicechat.adjust_volumes");
    private static final ITextComponent SELECT_MICROPHONE = new TranslationTextComponent("message.voicechat.select_microphone");
    private static final ITextComponent SELECT_SPEAKER = new TranslationTextComponent("message.voicechat.select_speaker");
    private static final ITextComponent BACK = new TranslationTextComponent("message.voicechat.back");

    @Nullable
    private final Screen parent;
    private VoiceActivationSlider voiceActivationSlider;
    private MicTestButton micTestButton;

    public VoiceChatSettingsScreen(@Nullable Screen parent) {
        super(TITLE, 248, 219);
        this.parent = parent;
    }

    public VoiceChatSettingsScreen() {
        this(null);
    }

    @Override
    protected void init() {

        super.init();
    }

    @Override
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        Round.draw(poseStack, new Rect(guiLeft, guiTop, xSize, ySize), 2, TempColor.getBackgroundColor());
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        int titleWidth =  Minecraft.getInstance().fontRenderer.getStringWidth(TITLE);
        Minecraft.getInstance().fontRenderer.drawShadow(poseStack, TITLE.getVisualOrderText(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, getFontColor());
        width = Minecraft.getInstance().getMainWindow().getScaledWidth();
        height = Minecraft.getInstance().getMainWindow().getScaledHeight(); font = Minecraft.getInstance().fontRenderer; minecraft = Minecraft.getInstance();

        int y = guiTop + 20;

        addButton(new VoiceSoundSlider(guiLeft + 10, y, xSize - 20, 20));
        y += 21;
        addButton(new MicAmplificationSlider(guiLeft + 10, y, xSize - 20, 20));
        y += 21;
        addButton(new DenoiserButton(guiLeft + 10, y, xSize - 20, 20));
        y += 21;

        voiceActivationSlider = new VoiceActivationSlider(guiLeft + 10 + 20 + 1, y , xSize - 20 - 20 - 1, 20);
        micTestButton = new MicTestButton(guiLeft + 10, y , voiceActivationSlider);
        addButton(new MicActivationButton(guiLeft + 10, y, xSize - 20, 20, type -> {
            voiceActivationSlider.visible = MicrophoneActivationType.VOICE.equals(type);
        }));

        addButton(micTestButton);
        addButton(voiceActivationSlider);
        y += 21 * 2;

        addButton(new EnumButton<AudioType>(guiLeft + 10, y, xSize - 20, 20, VoicechatClient.CLIENT_CONFIG.audioType) {

            @Override
            protected ITextComponent getText(AudioType type) {
                return new TranslationTextComponent("message.voicechat.audio_type", type.getText());
            }

            @Override
            protected void onUpdate(AudioType type) {
                ClientVoicechat client = ClientManager.getClient();
                if (client != null) {
                    micTestButton.stop();
                    client.reloadAudio();
                }
            }
        });
        y += 21;
        if (isIngame()) {
            addButton(new Button(guiLeft + 10, y, xSize - 20, 20, ADJUST_VOLUMES, button -> {
                Minecraft.getInstance().displayGuiScreen(new AdjustVolumesScreen());
            }));
            y += 21;
        }
        addButton(new Button(guiLeft + 10, y, xSize / 2 - 15, 20, SELECT_MICROPHONE, button -> {
            Minecraft.getInstance().displayGuiScreen(new SelectMicrophoneScreen(this));
        }));
        addButton(new Button(guiLeft + xSize / 2 + 1, y, (xSize - 20) / 2 - 1, 20, SELECT_SPEAKER, button -> {
            Minecraft.getInstance().displayGuiScreen(new SelectSpeakerScreen(this));
        }));
        y += 21;
        if (!isIngame() && parent != null) {
            addButton(new Button(guiLeft + 10, y, xSize - 20, 20, BACK, button -> {
                Minecraft.getInstance().displayGuiScreen(parent);
            }));
        }
        ITextComponent sliderTooltip = voiceActivationSlider.getHoverText();
        if (voiceActivationSlider.isHovered() && sliderTooltip != null) {
            renderTooltip(poseStack, sliderTooltip, mouseX, mouseY);
        } else if (micTestButton.isHovered()) {
            micTestButton.renderToolTip(poseStack, mouseX, mouseY);
        } /*else if (keybindButton.isHovered()) {
            renderTooltip(poseStack, ASSIGN_TOOLTIP, mouseX, mouseY);
        }*/
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        width = Minecraft.getInstance().getMainWindow().getScaledWidth();
        height = Minecraft.getInstance().getMainWindow().getScaledHeight(); font = Minecraft.getInstance().fontRenderer; minecraft = Minecraft.getInstance();

        int y = guiTop + 20;

        addButton(new VoiceSoundSlider(guiLeft + 10, y, xSize - 20, 20));
        y += 21;
        addButton(new MicAmplificationSlider(guiLeft + 10, y, xSize - 20, 20));
        y += 21;
        addButton(new DenoiserButton(guiLeft + 10, y, xSize - 20, 20));
        y += 21;

        voiceActivationSlider = new VoiceActivationSlider(guiLeft + 10, y + 21, xSize - 20, 20);
        micTestButton = new MicTestButton(guiLeft + 10, y, voiceActivationSlider);
        //keybindButton = new KeybindButton(KeyEvents.KEY_PTT, guiLeft + 10, y , xSize - 20 , 20, new TranslationTextComponent("Кнопка говорить: "));
        addButton(new MicActivationButton(guiLeft + 10, y, xSize - 20, 20, type -> {
            voiceActivationSlider.visible = MicrophoneActivationType.VOICE.equals(type);
            //keybindButton.visible = MicrophoneActivationType.PTT.equals(type);
        }));

        //    addButton(micTestButton);
        addButton(voiceActivationSlider);
        //  addButton(keybindButton);
        y += 21 * (voiceActivationSlider.visible ? 2 : 1);

        addButton(new EnumButton<AudioType>(guiLeft + 10, y, xSize - 20, 20, VoicechatClient.CLIENT_CONFIG.audioType) {

            @Override
            protected ITextComponent getText(AudioType type) {
                return new TranslationTextComponent("message.voicechat.audio_type", type.getText());
            }

            @Override
            protected void onUpdate(AudioType type) {
                ClientVoicechat client = ClientManager.getClient();
                if (client != null) {
                    micTestButton.stop();
                    client.reloadAudio();
                }
            }
        });
        y += 21;
        if (isIngame()) {
            addButton(new Button(guiLeft + 10, y, xSize - 20, 20, ADJUST_VOLUMES, button -> {
                Minecraft.getInstance().displayGuiScreen(new AdjustVolumesScreen());
            }));
            y += 21;
        }
        addButton(new Button(guiLeft + 10, y, xSize / 2 - 15, 20, SELECT_MICROPHONE, button -> {
            Minecraft.getInstance().displayGuiScreen(new SelectMicrophoneScreen(this));
        }));
        addButton(new Button(guiLeft + xSize / 2 + 1, y, (xSize - 20) / 2 - 1, 20, SELECT_SPEAKER, button -> {
            Minecraft.getInstance().displayGuiScreen(new SelectSpeakerScreen(this));
        }));
        y += 21;
        if (!isIngame() && parent != null) {
            addButton(new Button(guiLeft + 10, y, xSize - 20, 20, BACK, button -> {
                Minecraft.getInstance().displayGuiScreen(parent);
            }));
        }

    }

    @Override
    public boolean shouldCloseOnEsc() {
   /*     if (keybindButton.isListening()) {
            return false;
        }*/
        return super.shouldCloseOnEsc();
    }
}
