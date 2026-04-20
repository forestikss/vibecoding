package ru.etc1337.api.viamcp;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class VersionSelectScreen extends TextFieldWidget {

    public VersionSelectScreen(FontRenderer font, int x, int y, int width, int height, ITextComponent title) {
        super(font, x, y, width, height, title);
        setText(ProtocolVersion.getProtocol(ViaMCP.NATIVE_VERSION).getName());
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

        if (ProtocolVersion.getClosest(getText()) == null) {
            setTextColor(TextFormatting.RED.getColor());
        } else {
            ViaLoadingBase.getInstance().reload(ProtocolVersion.getClosest(getText()));
            setTextColor(TextFormatting.WHITE.getColor());
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}

