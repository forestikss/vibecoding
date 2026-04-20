package ru.etc1337.api.render.fonts.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.ITextComponent;
import ru.etc1337.api.color.FixColor;

import java.util.ArrayList;
import java.util.List;

public class TextBatcher {
    private final List<BatchEntry> batch = new ArrayList<>();
    private CFontRenderer fontRenderer;

    public TextBatcher(CFontRenderer fontRenderer) {
        this.fontRenderer = fontRenderer;
    }

    public void add(String text, MatrixStack matrixStack, float x, float y, int color, boolean shadow) {
        batch.add(new BatchEntry(text, matrixStack, x, y, color, shadow));
    }

    public void add(ITextComponent textComponent, MatrixStack matrixStack, float x, float y, float alpha) {
        batch.add(new BatchEntry(textComponent, matrixStack, x, y, alpha));
    }

    public void drawAll() {
        for (BatchEntry entry : batch) {
            if (entry.textComponent != null) {
                fontRenderer.drawGradient(entry.matrixStack, entry.textComponent, entry.x, entry.y, FixColor.WHITE.alpha(entry.alpha).getRGB());
            } else {
                fontRenderer.draw(entry.matrixStack, entry.text, entry.x, entry.y, entry.color);
            }
        }
        batch.clear();
    }

    public TextBatcher setFont(CFontRenderer font) {
        fontRenderer = font;
        return this;
    }

    private static class BatchEntry {
        String text;
        ITextComponent textComponent;
        MatrixStack matrixStack;
        float x, y;
        int color;
        float alpha;
        boolean shadow;

        BatchEntry(String text, MatrixStack matrixStack, float x, float y, int color, boolean shadow) {
            this.text = text;
            this.matrixStack = matrixStack;
            this.x = x;
            this.y = y;
            this.color = color;
            this.shadow = shadow;
        }

        BatchEntry(ITextComponent textComponent, MatrixStack matrixStack, float x, float y, float alpha) {
            this.textComponent = textComponent;
            this.matrixStack = matrixStack;
            this.x = x;
            this.y = y;
            this.alpha = alpha;
        }
    }
}
