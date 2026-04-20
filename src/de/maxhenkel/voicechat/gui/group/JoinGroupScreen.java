package de.maxhenkel.voicechat.gui.group;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.CreateGroupScreen;
import de.maxhenkel.voicechat.gui.EnterPasswordScreen;
import de.maxhenkel.voicechat.gui.widgets.ListScreenBase;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.shaders.impl.Round;


public class JoinGroupScreen extends ListScreenBase {

    protected static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/gui_join_group.png");
    protected static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.join_create_group.title");
    protected static final ITextComponent CREATE_GROUP = new TranslationTextComponent("message.voicechat.create_group_button");
    protected static final ITextComponent JOIN_CREATE_GROUP = new TranslationTextComponent("message.voicechat.join_create_group");
    protected static final ITextComponent NO_GROUPS = new TranslationTextComponent("message.voicechat.no_groups").mergeStyle(TextFormatting.GRAY);

    protected static final int HEADER_SIZE = 16;
    protected static final int FOOTER_SIZE = 32;
    protected static final int UNIT_SIZE = 18;
    protected static final int CELL_HEIGHT = 36;

    protected JoinGroupList groupList;
    protected Button createGroup;
    protected int units;

    public JoinGroupScreen() {
        super(TITLE, 236, 0);
    }

    @Override
    protected void init() {

        super.init();
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        width = Minecraft.getInstance().getMainWindow().getScaledWidth();
        height = Minecraft.getInstance().getMainWindow().getScaledHeight(); font = Minecraft.getInstance().fontRenderer; minecraft = Minecraft.getInstance();
        guiLeft = guiLeft + 2;
        guiTop = 32;
        int minUnits = MathHelper.ceil((float) (CELL_HEIGHT + 4) / (float) UNIT_SIZE);
        units = Math.max(minUnits, (height - HEADER_SIZE - FOOTER_SIZE - guiTop * 2) / UNIT_SIZE);
        ySize = HEADER_SIZE + units * UNIT_SIZE + FOOTER_SIZE;

        if (groupList != null) {
            groupList.updateSize(width, units * UNIT_SIZE, guiTop + HEADER_SIZE);
        } else {
            groupList = new JoinGroupList(this, width, units * UNIT_SIZE, guiTop + HEADER_SIZE, CELL_HEIGHT);
        }
        addListener(groupList);

        createGroup = new Button(guiLeft + 7, guiTop + ySize - 20 - 7, xSize - 14, 20, CREATE_GROUP, button -> {
            Minecraft.getInstance().displayGuiScreen(new CreateGroupScreen());
        });
        addButton(createGroup);
        
    }

    @Override
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        Round.draw(poseStack, new Rect(guiLeft, guiTop, xSize, (HEADER_SIZE + UNIT_SIZE * units) + FOOTER_SIZE), 2, TempColor.getBackgroundColor());
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
         Minecraft.getInstance().fontRenderer.drawShadow(poseStack, JOIN_CREATE_GROUP, guiLeft + xSize / 2 -  Minecraft.getInstance().fontRenderer.getStringWidth(JOIN_CREATE_GROUP) / 2, guiTop + 5, FONT_COLOR);

        if (!groupList.isEmpty()) {
            groupList.render(poseStack, mouseX, mouseY, delta);
        } else {
            drawCenteredString(poseStack, font, NO_GROUPS, width / 2, guiTop + HEADER_SIZE + (units * UNIT_SIZE) / 2 -  Minecraft.getInstance().fontRenderer.lineHeight / 2, -1);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        for (JoinGroupEntry entry : groupList.getEventListeners()) {
            if (entry.isMouseOver(mouseX, mouseY)) {
                ClientGroup group = entry.getGroup().getGroup();
                minecraft.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1F));
                if (group.hasPassword()) {
                    Minecraft.getInstance().displayGuiScreen(new EnterPasswordScreen(group));
                } else {
                }
                return true;
            }
        }
        return false;
    }

}
