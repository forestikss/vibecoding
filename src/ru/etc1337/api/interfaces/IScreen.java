package ru.etc1337.api.interfaces;


import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;

public interface IScreen {
    void resize(Minecraft minecraft, int width, int height);

    void init();

    void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks);

    boolean mouseClicked(double mouseX, double mouseY, int button);

    boolean mouseReleased(double mouseX, double mouseY, int button);

    boolean keyPressed(int keyCode, int scanCode, int modifiers);

    boolean keyReleased(int keyCode, int scanCode, int modifiers);

    boolean charTyped(char codePoint, int modifiers);

    void onClose();
}