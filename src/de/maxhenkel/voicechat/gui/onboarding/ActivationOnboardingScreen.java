package de.maxhenkel.voicechat.gui.onboarding;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.VoiceChatScreen;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class ActivationOnboardingScreen extends OnboardingScreenBase {

    private static final ITextComponent TITLE = new TranslationTextComponent("message.voicechat.onboarding.activation.title").mergeStyle(TextFormatting.BOLD);
    private static final ITextComponent DESCRIPTION = new TranslationTextComponent("message.voicechat.onboarding.activation")
            .append(ITextComponent.getTextComponentOrEmpty("\n\n"))
            .append(new TranslationTextComponent("message.voicechat.onboarding.activation.ptt", new TranslationTextComponent("message.voicechat.onboarding.activation.ptt.name").mergeStyle(TextFormatting.BOLD, TextFormatting.UNDERLINE)))
            .append(ITextComponent.getTextComponentOrEmpty("\n\n"))
            .append(new TranslationTextComponent("message.voicechat.onboarding.activation.voice", new TranslationTextComponent("message.voicechat.onboarding.activation.voice.name").mergeStyle(TextFormatting.BOLD, TextFormatting.UNDERLINE)));

    public ActivationOnboardingScreen(@Nullable Screen previous) {
        super(TITLE, previous);
    }

    @Override
    protected void init() {

        super.init();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
/*        width = Minecraft.getInstance().getMainWindow().getScaledWidth();
        height = Minecraft.getInstance().getMainWindow().getScaledHeight(); font = Minecraft.getInstance().font; minecraft = Minecraft.getInstance();

        Button ptt = new Button(guiLeft, guiTop + contentHeight - BUTTON_HEIGHT * 2 - PADDING, contentWidth / 2 - PADDING / 2, BUTTON_HEIGHT, new TranslationTextComponent("message.voicechat.onboarding.activation.ptt.name"), button -> {
            VoicechatClient.CLIENT_CONFIG.microphoneActivationType.set(MicrophoneActivationType.PTT).save();
            Minecraft.getInstance().displayGuiScreen(new PttOnboardingScreen(this));
        });
        addButton(ptt);

        Button voice = new Button(guiLeft + contentWidth / 2 + PADDING / 2, guiTop + contentHeight - BUTTON_HEIGHT * 2 - PADDING, contentWidth / 2 - PADDING / 2, BUTTON_HEIGHT, new TranslationTextComponent("message.voicechat.onboarding.activation.voice.name"), button -> {
            VoicechatClient.CLIENT_CONFIG.microphoneActivationType.set(MicrophoneActivationType.VOICE).save();
            Minecraft.getInstance().displayGuiScreen(new VoiceActivationOnboardingScreen(this));
        });
        addButton(voice);

        addBackOrCancelButton(true);
        renderTitle(stack, TITLE);
        renderMultilineText(stack, DESCRIPTION);*/
        OnboardingManager.finishOnboarding();
        Minecraft.getInstance().displayGuiScreen(new VoiceChatScreen());
    }
}
