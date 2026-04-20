package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.Voicechat;
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


public class CreateGroupScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/gui_create_group.png");
    private static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.create_group.title");
    private static final ITextComponent CREATE = new TranslationTextComponent("message.voicechat.create");
    private static final ITextComponent CREATE_GROUP = new TranslationTextComponent("message.voicechat.create_group");
    private static final ITextComponent GROUP_NAME = new TranslationTextComponent("message.voicechat.group_name");
    private static final ITextComponent OPTIONAL_PASSWORD = new TranslationTextComponent("message.voicechat.optional_password");
    private static final ITextComponent GROUP_TYPE = new TranslationTextComponent("message.voicechat.group_type");

    private TextFieldWidget groupName;
    private TextFieldWidget password;
    private GroupType groupType;
    private Button groupTypeButton;
    private Button createGroup;

    public CreateGroupScreen() {
        super(TITLE, 195, 124);
        groupType = GroupType.NORMAL;
    }

    @Override
    protected void init() {

        super.init();
    }

    private void createGroup() {
        if (!groupName.getText().isEmpty()) {
        }
    }

    @Override
    public void tick() {
        super.tick();
        groupName.tick();
        password.tick();
        createGroup.active = !groupName.getText().isEmpty();
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
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
         Minecraft.getInstance().fontRenderer.drawShadow(poseStack, CREATE_GROUP, guiLeft + xSize / 2 -  Minecraft.getInstance().fontRenderer.getStringWidth(CREATE_GROUP) / 2, guiTop + 7, FONT_COLOR);
         Minecraft.getInstance().fontRenderer.drawShadow(poseStack, GROUP_NAME, guiLeft + 8, guiTop + 7 +  Minecraft.getInstance().fontRenderer.lineHeight + 5, FONT_COLOR);
         Minecraft.getInstance().fontRenderer.drawShadow(poseStack, OPTIONAL_PASSWORD, guiLeft + 8, guiTop + 7 + ( Minecraft.getInstance().fontRenderer.lineHeight + 5) * 2 + 10 + 2, FONT_COLOR);

        if (mouseX >= groupTypeButton.x && mouseY >= groupTypeButton.y && mouseX < groupTypeButton.x + groupTypeButton.getWidth() && mouseY < groupTypeButton.y + groupTypeButton.getHeight()) {
            renderTooltip(poseStack, Minecraft.getInstance().fontRenderer.trimStringToWidth(groupType.getDescription(), 200), mouseX, mouseY);
        }
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
            createGroup();
            return true;
        }
        return false;
    }

    @Override
    public void resize(Minecraft client, int width, int height) {
        String groupNameText = groupName.getText();
        String passwordText = password.getText();
        init(client, width, height);
        groupName.setText(groupNameText);
        password.setText(passwordText);
    }

}
