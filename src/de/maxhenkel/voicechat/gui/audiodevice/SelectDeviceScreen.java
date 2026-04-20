package de.maxhenkel.voicechat.gui.audiodevice;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.widgets.ListScreenBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.shaders.impl.Round;

import javax.annotation.Nullable;
import java.util.List;

public abstract class SelectDeviceScreen extends ListScreenBase {

    protected static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/gui_audio_devices.png");
    protected static final ITextComponent BACK = new TranslationTextComponent("message.voicechat.back");

    protected static final int HEADER_SIZE = 16;
    protected static final int FOOTER_SIZE = 32;
    protected static final int UNIT_SIZE = 18;


    @Nullable
    protected Screen parent;
    protected AudioDeviceList deviceList;
    protected Button back;
    protected int units;

    public SelectDeviceScreen(ITextComponent title, @Nullable Screen parent) {
        super(title, 236, 0);
        this.parent = parent;
    }

    public abstract List<String> getDevices();

    public abstract ResourceLocation getIcon();

    public abstract ITextComponent getEmptyListComponent();

    public abstract ConfigEntry<String> getConfigEntry();

    @Override
    protected void init() {

        super.init();
    }


    @Override
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        if (isIngame()) {
            Round.draw(poseStack, new Rect(guiLeft, guiTop, xSize, (HEADER_SIZE + UNIT_SIZE * units) + FOOTER_SIZE), 2, TempColor.getBackgroundColor());
        }
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        width = Minecraft.getInstance().getMainWindow().getScaledWidth();
        height = Minecraft.getInstance().getMainWindow().getScaledHeight(); font = Minecraft.getInstance().fontRenderer; minecraft = Minecraft.getInstance();
        guiLeft = guiLeft + 2;
        guiTop = 32;
        int minUnits = MathHelper.ceil((float) (AudioDeviceList.CELL_HEIGHT + 4) / (float) UNIT_SIZE);
        units = Math.max(minUnits, (height - HEADER_SIZE - FOOTER_SIZE - guiTop * 2) / UNIT_SIZE);
        ySize = HEADER_SIZE + units * UNIT_SIZE + FOOTER_SIZE;

        if (deviceList != null) {
            deviceList.updateSize(width, units * UNIT_SIZE, guiTop + HEADER_SIZE);
        } else {
            deviceList = new AudioDeviceList(width, units * UNIT_SIZE, guiTop + HEADER_SIZE).setIcon(getIcon()).setConfigEntry(getConfigEntry());
        }
        addListener(deviceList);

        back = new Button(guiLeft + 7, guiTop + ySize - 20 - 7, xSize - 14, 20, BACK, button -> {
            Minecraft.getInstance().displayGuiScreen(parent);
        });
        addButton(back);

        deviceList.setAudioDevices(getDevices());
        
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        super.renderForeground(poseStack, mouseX, mouseY, delta);
         Minecraft.getInstance().fontRenderer.drawShadow(poseStack, title, width / 2 -  Minecraft.getInstance().fontRenderer.getStringWidth(title) / 2, guiTop + 5, isIngame() ? FONT_COLOR : TextFormatting.WHITE.getColor());
        width = Minecraft.getInstance().getMainWindow().getScaledWidth();
        height = Minecraft.getInstance().getMainWindow().getScaledHeight(); font = Minecraft.getInstance().fontRenderer; minecraft = Minecraft.getInstance();
        guiLeft = guiLeft + 2;
        guiTop = 32;
        int minUnits = MathHelper.ceil((float) (AudioDeviceList.CELL_HEIGHT + 4) / (float) UNIT_SIZE);
        units = Math.max(minUnits, (height - HEADER_SIZE - FOOTER_SIZE - guiTop * 2) / UNIT_SIZE);
        ySize = HEADER_SIZE + units * UNIT_SIZE + FOOTER_SIZE;

        if (deviceList != null) {
            deviceList.updateSize(width, units * UNIT_SIZE, guiTop + HEADER_SIZE);
        } else {
            deviceList = new AudioDeviceList(width, units * UNIT_SIZE, guiTop + HEADER_SIZE).setIcon(getIcon()).setConfigEntry(getConfigEntry());
        }
        addListener(deviceList);

        back = new Button(guiLeft + 7, guiTop + ySize - 20 - 7, xSize - 14, 20, BACK, button -> {
            Minecraft.getInstance().displayGuiScreen(parent);
        });
        addButton(back);

        deviceList.setAudioDevices(getDevices());
        if (!deviceList.isEmpty()) {
            deviceList.render(poseStack, mouseX, mouseY, delta);
        } else {
            drawCenteredString(poseStack, font, getEmptyListComponent(), width / 2, guiTop + HEADER_SIZE + (units * UNIT_SIZE) / 2 -  Minecraft.getInstance().fontRenderer.lineHeight / 2, -1);
        }
    }

}
