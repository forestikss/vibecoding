package ru.etc1337.api.render.fonts.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.fonts.v2.FontData;
import ru.etc1337.api.render.fonts.v2.MsdfFont;
import ru.etc1337.api.render.shaders.Shader;
import ru.etc1337.api.render.shaders.impl.Msdf;

import java.awt.*;

import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR_TEX;

public class CFontRenderer extends Shader {
    private final MsdfFont font;
    private final float size;

    public CFontRenderer(String name, float size) {
        this(name + ".png", name + ".json", size);
    }

    public CFontRenderer(String atlas, String data, float size) {
        font = MsdfFont.builder().withAtlas(atlas).withData(data).build();
        this.size = size;
    }

    public void draw(MatrixStack stack, String text, float x, float y, int color) {
        y += 1.5f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        FontData.AtlasData atlas = this.font.getAtlas();

        int currentColor = color;
        float currentX = x;
        StringBuilder currentText = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§') {
                if (i + 1 < text.length()) {
                    char formattingCode = text.charAt(i + 1);
                    TextFormatting formatting = TextFormatting.fromFormattingCode(formattingCode);
                    if (formatting != null) {
                        // Draw accumulated text before changing color
                        if (!currentText.isEmpty()) {
                            drawFormattedText(stack, currentText.toString(), currentX, y, currentColor, atlas);
                            currentX += width(currentText.toString());
                            currentText.setLength(0);
                        }

                        if (formatting.isColor()) {
                            Integer formatColor = formatting.getColor();
                            if (formatColor != null) {
                                // Preserve alpha from original color
                                currentColor = (color & 0xFF000000) | (formatColor & 0x00FFFFFF);
                            }
                        }
                        i++; // Skip the formatting code
                        continue;
                    }
                }
            }
            currentText.append(c);
        }

        // Draw remaining text
        if (!currentText.isEmpty()) {
            drawFormattedText(stack, currentText.toString(), currentX, y, currentColor, atlas);
        }
    }

    private void drawFormattedText(MatrixStack stack, String text, float x, float y, int color, FontData.AtlasData atlas) {
        int red = (color >> 16) & 255;
        int green = (color >> 8) & 255;
        int blue = color & 255;
        int alpha = (color >> 24) & 255;

        Msdf.draw(() -> {
                    this.font.bind();
                    builder.begin(GL11.GL_QUADS, POSITION_COLOR_TEX);
                    this.font.applyGlyphs(stack.getLast().getMatrix(), builder, size, text, 0,
                            x, y + font.getMetrics().baselineHeight() * size, 0, red, green, blue, alpha);
                    tessellator.draw();
                    this.font.unbind();
                }, stack, new Rect(0, 0, atlas.width(), atlas.height()), atlas.range(), 0.5f, 0F,
                new FixColor(red, green, blue, alpha), false, 0, FixColor.BLACK);
    }

    public void drawCenter(MatrixStack stack, String text, float x, float y, int color) {
        draw(stack, text, x - width(text) / 2F, y, color);
    }


    public void drawGradientCenter(MatrixStack stack, ITextComponent text, float x, float y, int color) {
        drawGradient(stack, text, x - width(text) / 2F, y, color);
    }

    public void drawGradient(MatrixStack stack, ITextComponent text, float x, float y, int color) {
        float offset = 0;
        int alpha = (color >> 24) & 0xFF;

        for (ITextComponent it : text.getSiblings()) {
            for (ITextComponent it1 : it.getSiblings()) {
                String draw = it1.getString();
                int styleColor = it1.getStyle().getColor() != null ? it1.getStyle().getColor().getColor() : color;
                int finalColor = (alpha << 24) | (styleColor & 0x00FFFFFF);

                this.draw(stack, draw, (float) (x + offset), (float) y, finalColor);
                offset += width(draw) - 0.15f;
            }

            if (it.getSiblings().size() <= 1) {
                String draw = it.getString();
                int styleColor = it.getStyle().getColor() != null ? it.getStyle().getColor().getColor() : Color.WHITE.getRGB();
                int finalColor = (alpha << 24) | (styleColor & 0x00FFFFFF);

                this.draw(stack, draw, (float) (x + offset), (float) y, finalColor);
                offset += width(draw) - 0.15f;
            }
        }

    }

    public void drawOutline(MatrixStack stack, String text, float x, float y, int color) {
        draw(stack, text, x - 0.5F, y, Color.BLACK.getRGB());
        draw(stack, text, x + 0.5F, y, Color.BLACK.getRGB());
        draw(stack, text, x, y - 0.5F, Color.BLACK.getRGB());
        draw(stack, text, x, y + 0.5F, Color.BLACK.getRGB());
        draw(stack, text, x, y, color);
    }

    public void drawCenterOutline(MatrixStack stack, String text, float x, float y, int color) {
        drawCenter(stack, text, x - 0.5F, y, Color.BLACK.getRGB());
        drawCenter(stack, text, x + 0.5F, y, Color.BLACK.getRGB());
        drawCenter(stack, text, x, y - 0.5F, Color.BLACK.getRGB());
        drawCenter(stack, text, x, y + 0.5F, Color.BLACK.getRGB());
        drawCenter(stack, text, x, y, color);
    }

    public float width(ITextComponent text) {
        float offset = 0;

        for (ITextComponent it : text.getSiblings()) {
            for (ITextComponent it1 : it.getSiblings()) {
                String draw = it1.getString();
                offset += width(draw) - 0.15f;
            }

            if (it.getSiblings().size() <= 1) {
                String draw = it.getString();
                offset += width(draw) - 0.15f;
            }
        }

        if (text.getSiblings().isEmpty()) {
            String draw = TextFormatting.getTextWithoutFormattingCodes(text.getString());
            if (draw != null) offset += width(draw);
        }

        return offset;
    }

    public float width(String text) {
        return font.getWidth(text, size) + 1f;
    }

    public float width(String text, float thickness) {
        return font.getWidth(text, size, thickness);
    }

    public float height() {
        return this.size; // this.size
    }

}
