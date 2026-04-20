package de.maxhenkel.voicechat.gui.volume;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import de.maxhenkel.voicechat.gui.widgets.ListScreenBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.shaders.impl.Round;


import java.util.Locale;

public class AdjustVolumesScreen extends ListScreenBase {

    protected static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/gui_volumes.png");
    protected static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.adjust_volume.title");
    protected static final ITextComponent SEARCH_HINT = new TranslationTextComponent("message.voicechat.search_hint").mergeStyle(TextFormatting.ITALIC).mergeStyle(TextFormatting.GRAY);
    protected static final ITextComponent EMPTY_SEARCH = new TranslationTextComponent("message.voicechat.search_empty").mergeStyle(TextFormatting.GRAY);

    protected static final int HEADER_SIZE = 16;
    protected static final int FOOTER_SIZE = 8;
    protected static final int SEARCH_HEIGHT = 16;
    protected static final int UNIT_SIZE = 18;
    protected static final int CELL_HEIGHT = 36;

    protected AdjustVolumeList volumeList;
    protected TextFieldWidget searchBox;
    protected String lastSearch;
    protected int units;

    public AdjustVolumesScreen() {
        super(TITLE, 236, 0);
        this.lastSearch = "";
    }

    @Override
    public void tick() {
        super.tick();
        if (searchBox != null) {
            searchBox.tick();
        }
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        Round.draw(poseStack, new Rect(guiLeft, guiTop, xSize, (HEADER_SIZE + UNIT_SIZE * units) + FOOTER_SIZE), 2, TempColor.getBackgroundColor());
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        Minecraft.getInstance().fontRenderer.drawShadow(poseStack, TITLE, width / 2 - Minecraft.getInstance().fontRenderer.getStringWidth(TITLE) / 2, guiTop + 5, VoiceChatScreenBase.FONT_COLOR);
        width = Minecraft.getInstance().getMainWindow().getScaledWidth();
        height = Minecraft.getInstance().getMainWindow().getScaledHeight();
        font = Minecraft.getInstance().fontRenderer;
        minecraft = Minecraft.getInstance();

        guiLeft = guiLeft + 2;
        guiTop = 32;
        int minUnits = MathHelper.ceil((float) (CELL_HEIGHT + SEARCH_HEIGHT + 4) / (float) UNIT_SIZE);
        units = Math.max(minUnits, (height - HEADER_SIZE - FOOTER_SIZE - guiTop * 2 - SEARCH_HEIGHT) / UNIT_SIZE);
        ySize = HEADER_SIZE + units * UNIT_SIZE + FOOTER_SIZE;

        if (volumeList != null) {
            volumeList.updateSize(width, units * UNIT_SIZE - SEARCH_HEIGHT, guiTop + HEADER_SIZE + SEARCH_HEIGHT);
        } else {
            volumeList = new AdjustVolumeList(width, units * UNIT_SIZE - SEARCH_HEIGHT, guiTop + HEADER_SIZE + SEARCH_HEIGHT, CELL_HEIGHT, this);
        }
        String string = (searchBox != null) ? searchBox.getText() : "";
        searchBox = new TextFieldWidget(font, guiLeft + 28, guiTop + HEADER_SIZE + 6, 196, SEARCH_HEIGHT, SEARCH_HINT);
        searchBox.setMaxStringLength(16);
        searchBox.setVisible(true);
        searchBox.setTextColor(0xFFFFFF);
        searchBox.setText(string);
        searchBox.setResponder(this::checkSearchStringUpdate);
        addListener(searchBox);
        addListener(volumeList);

        if (!volumeList.isEmpty()) {
            volumeList.render(poseStack, mouseX, mouseY, delta);
        } else if (searchBox != null && !searchBox.getText().isEmpty()) {
            drawCenteredString(poseStack, font, EMPTY_SEARCH, width / 2, guiTop + HEADER_SIZE + (units * UNIT_SIZE) / 2 - Minecraft.getInstance().fontRenderer.lineHeight / 2, -1);
        }
        if (searchBox != null && !searchBox.isFocused() && searchBox.getText().isEmpty()) {
            drawString(poseStack, font, SEARCH_HINT, searchBox.x, searchBox.y, -1);
        } else if (searchBox != null) {
            searchBox.render(poseStack, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (searchBox != null && searchBox.isFocused()) {
            searchBox.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button) || (volumeList != null && volumeList.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        width = Minecraft.getInstance().getMainWindow().getScaledWidth();
        height = Minecraft.getInstance().getMainWindow().getScaledHeight();
        font = Minecraft.getInstance().fontRenderer;
        minecraft = Minecraft.getInstance();

        guiLeft = guiLeft + 2;
        guiTop = 32;
        int minUnits = MathHelper.ceil((float) (CELL_HEIGHT + SEARCH_HEIGHT + 4) / (float) UNIT_SIZE);
        units = Math.max(minUnits, (height - HEADER_SIZE - FOOTER_SIZE - guiTop * 2 - SEARCH_HEIGHT) / UNIT_SIZE);
        ySize = HEADER_SIZE + units * UNIT_SIZE + FOOTER_SIZE;

        if (volumeList != null) {
            volumeList.updateSize(width, units * UNIT_SIZE - SEARCH_HEIGHT, guiTop + HEADER_SIZE + SEARCH_HEIGHT);
        } else {
            volumeList = new AdjustVolumeList(width, units * UNIT_SIZE - SEARCH_HEIGHT, guiTop + HEADER_SIZE + SEARCH_HEIGHT, CELL_HEIGHT, this);
        }
        String string = (searchBox != null) ? searchBox.getText() : "";
        searchBox = new TextFieldWidget(font, guiLeft + 28, guiTop + HEADER_SIZE + 6, 196, SEARCH_HEIGHT, SEARCH_HINT);
        searchBox.setMaxStringLength(16);
        searchBox.setVisible(true);
        searchBox.setTextColor(0xFFFFFF);
        searchBox.setText(string);
        searchBox.setResponder(this::checkSearchStringUpdate);
        addListener(searchBox);
        addListener(volumeList);
        
    }

    private void checkSearchStringUpdate(String string) {
        if (!(string = string.toLowerCase(Locale.ROOT)).equals(lastSearch)) {
            volumeList.setFilter(string);
            lastSearch = string;
        }
    }

}