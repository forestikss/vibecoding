package de.maxhenkel.voicechat.gui.group;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.gui.GroupType;
import de.maxhenkel.voicechat.gui.tooltips.DisableTooltipSupplier;
import de.maxhenkel.voicechat.gui.tooltips.HideGroupHudTooltipSupplier;
import de.maxhenkel.voicechat.gui.tooltips.MuteTooltipSupplier;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.gui.widgets.ListScreenBase;
import de.maxhenkel.voicechat.gui.widgets.ToggleImageButton;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.shaders.impl.Round;


public class GroupScreen extends ListScreenBase {

    protected static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/gui_group.png");
    protected static final ResourceLocation LEAVE = new ResourceLocation("minecraft", "textures/icons/leave.png");
    protected static final ResourceLocation MICROPHONE = new ResourceLocation("minecraft", "textures/icons/microphone_button.png");
    protected static final ResourceLocation SPEAKER = new ResourceLocation("minecraft", "textures/icons/speaker_button.png");
    protected static final ResourceLocation GROUP_HUD = new ResourceLocation("minecraft", "textures/icons/group_hud_button.png");
    protected static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.group.title");
    protected static final ITextComponent LEAVE_GROUP = new TranslationTextComponent("message.voicechat.leave_group");

    protected static final int HEADER_SIZE = 16;
    protected static final int FOOTER_SIZE = 32;
    protected static final int UNIT_SIZE = 18;
    protected static final int CELL_HEIGHT = 36;

    protected GroupList groupList;
    protected int units;

    protected final ClientGroup group;
    protected ToggleImageButton mute;
    protected ToggleImageButton disable;
    protected ToggleImageButton showHUD;
    protected ImageButton leave;

    public GroupScreen(ClientGroup group) {
        super(TITLE, 236, 0);
        this.group = group;
    }

    @Override
    protected void init() {

        super.init();
    }

    @Override
    public void tick() {
        super.tick();
        checkButtons();
    }

    private void checkButtons() {
        mute.active = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE);
        showHUD.active = !VoicechatClient.CLIENT_CONFIG.hideIcons.get();
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

        ClientPlayerStateManager stateManager = ClientManager.getPlayerStateManager();

        if (groupList != null) {
            groupList.updateSize(width, units * UNIT_SIZE, guiTop + HEADER_SIZE);
        } else {
            groupList = new GroupList(this, width, units * UNIT_SIZE, guiTop + HEADER_SIZE, CELL_HEIGHT);
        }
        addListener(groupList);

        int buttonY = guiTop + ySize - 20 - 7;
        int buttonSize = 20;

        mute = new ToggleImageButton(guiLeft + 7, buttonY, MICROPHONE, stateManager::isMuted, button -> {
            stateManager.setMuted(!stateManager.isMuted());
        }, new MuteTooltipSupplier(this, stateManager));
        addButton(mute);

        disable = new ToggleImageButton(guiLeft + 7 + buttonSize + 3, buttonY, SPEAKER, stateManager::isDisabled, button -> {
            stateManager.setDisabled(!stateManager.isDisabled());
        }, new DisableTooltipSupplier(this, stateManager));
        addButton(disable);

        showHUD = new ToggleImageButton(guiLeft + 7 + (buttonSize + 3) * 2, buttonY, GROUP_HUD, VoicechatClient.CLIENT_CONFIG.showGroupHUD::get, button -> {
            VoicechatClient.CLIENT_CONFIG.showGroupHUD.set(!VoicechatClient.CLIENT_CONFIG.showGroupHUD.get()).save();
        }, new HideGroupHudTooltipSupplier(this));
        addButton(showHUD);



        checkButtons();
        
    }

    @Override
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        Round.draw(poseStack, new Rect(guiLeft, guiTop, xSize, (HEADER_SIZE + UNIT_SIZE * units) + FOOTER_SIZE), 2, TempColor.getBackgroundColor());
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        ITextComponent title;
        if (group.getType().equals(Group.Type.NORMAL)) {
            title = new TranslationTextComponent("message.voicechat.group_title", new StringTextComponent(group.getName()));
        } else {
            title = new TranslationTextComponent("message.voicechat.group_type_title", new StringTextComponent(group.getName()), GroupType.fromType(group.getType()).getTranslation());
        }

         Minecraft.getInstance().fontRenderer.drawShadow(poseStack, title, guiLeft + xSize / 2 -  Minecraft.getInstance().fontRenderer.getStringWidth(title) / 2, guiTop + 5, FONT_COLOR);

        groupList.render(poseStack, mouseX, mouseY, delta);
    }

}
