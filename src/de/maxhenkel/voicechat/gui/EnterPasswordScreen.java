package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.shaders.impl.Round;


public class EnterPasswordScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/gui_enter_password.png");
    private static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.enter_password.title");
    private static final ITextComponent JOIN_GROUP = new TranslationTextComponent("message.voicechat.join_group");
    private static final ITextComponent ENTER_GROUP_PASSWORD = new TranslationTextComponent("message.voicechat.enter_group_password");
    private static final ITextComponent PASSWORD = new TranslationTextComponent("message.voicechat.password");

    private TextFieldWidget password;
    private Button joinGroup;
    private ClientGroup group;

    public EnterPasswordScreen(ClientGroup group) {
        super(TITLE, 195, 74);
        this.group = group;
    }

    @Override
    protected void init() {

        super.init();
    }

    private void joinGroup() {
        if (!password.getText().isEmpty()) {
        }
    }

    @Override
    public void tick() {
        super.tick();
        password.tick();
        joinGroup.active = !password.getText().isEmpty();
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        Round.draw(poseStack, new Rect(guiLeft, guiTop, xSize, ySize), 2, TempColor.getBackgroundColor());
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
         Minecraft.getInstance().fontRenderer.drawShadow(poseStack, ENTER_GROUP_PASSWORD, guiLeft + xSize / 2 -  Minecraft.getInstance().fontRenderer.getStringWidth(ENTER_GROUP_PASSWORD) / 2, guiTop + 7, FONT_COLOR);
         Minecraft.getInstance().fontRenderer.drawShadow(poseStack, PASSWORD, guiLeft + 8, guiTop + 7 +  Minecraft.getInstance().fontRenderer.lineHeight + 5, FONT_COLOR);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().displayGuiScreen(null);
            return true;
        }
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            joinGroup();
            return true;
        }
        return false;
    }

    @Override
    public void resize(Minecraft client, int width, int height) {
        String passwordText = password.getText();
        init(client, width, height);
        password.setText(passwordText);
    }

}
