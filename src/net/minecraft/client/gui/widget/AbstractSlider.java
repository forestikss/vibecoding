package net.minecraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class AbstractSlider extends Widget
{
    protected double value;

    public AbstractSlider(int x, int y, int width, int height, ITextComponent message, double defaultValue)
    {
        super(x, y, width, height, message);
        this.value = defaultValue;
    }

    protected int getYImage(boolean isHovered)
    {
        return 0;
    }

    protected IFormattableTextComponent getNarrationMessage()
    {
        return new TranslationTextComponent("gui.narrate.slider", this.getMessage());
    }

    protected void renderBg(MatrixStack matrixStack, Minecraft minecraft, int mouseX, int mouseY)
    {
        minecraft.getTextureManager().bindTexture(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.isHovered() ? 2 : 1) * 20;
        this.blit(matrixStack, this.x + (int)(this.value * (double)(this.width - 8)), this.y, 0, 46 + i, 4, 20);
        this.blit(matrixStack, this.x + (int)(this.value * (double)(this.width - 8)) + 4, this.y, 196, 46 + i, 4, 20);
    }

    public void onClick(double mouseX, double mouseY)
    {
        this.changeSliderValue(mouseX);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        boolean flag = keyCode == 263;

        if (flag || keyCode == 262)
        {
            float f = flag ? -1.0F : 1.0F;
            this.setValue(this.value + (double)(f / (float)(this.width - 8)));
        }

        return false;
    }

    private void changeSliderValue(double mouseX)
    {
        this.setValue((mouseX - (double)(this.x + 4)) / (double)(this.width - 8));
    }

    private void setValue(double value)
    {
        double d0 = this.value;
        this.value = MathHelper.clamp(value, 0.0D, 1.0D);

        if (d0 != this.value)
        {
            this.applyValue();
        }

        this.updateMessage();
    }

    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY)
    {
        this.changeSliderValue(mouseX);
        super.onDrag(mouseX, mouseY, dragX, dragY);
    }

    public void playDownSound(SoundHandler handler)
    {
    }

    public void onRelease(double mouseX, double mouseY)
    {
        super.playDownSound(Minecraft.getInstance().getSoundHandler());
    }

    protected abstract void updateMessage();

    protected abstract void applyValue();
}
