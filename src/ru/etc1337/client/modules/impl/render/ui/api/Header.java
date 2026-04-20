package ru.etc1337.client.modules.impl.render.ui.api;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.draggable.Draggable;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.fonts.impl.CFontRenderer;
import ru.etc1337.api.render.shaders.impl.CircularProgressBar;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.render.shaders.impl.Outline;
import ru.etc1337.client.modules.impl.render.Interface;
import ru.etc1337.client.modules.impl.render.ui.settings.Settings;

@UtilityClass
public class Header implements QuickImports {
    private static final Settings settings = Client.getInstance().getModuleManager().get(Interface.class).find(Settings.class);

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text) {
        drawModernHeader(matrixStack, draggable, x, y, icon, text, new FixColor(TempColor.getBackgroundColor().darker().darker()));
    }
    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, double alpha) {
        drawModernHeader(matrixStack, draggable, x, y, icon, text, new FixColor(TempColor.getBackgroundColor().darker().darker()), alpha);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, float scale, double alpha) {
        drawModernHeader(matrixStack, draggable, x, y, icon, text, new FixColor(TempColor.getBackgroundColor().darker().darker()), scale, alpha);
    }
    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, float scale, double alpha, String string2Circle) {
        drawModernHeader(matrixStack, draggable, x, y, icon, text, new FixColor(TempColor.getBackgroundColor().darker().darker()), scale, alpha, string2Circle);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, float scale, double alpha, FixColor iconColor) {
        drawModernHeader(matrixStack, draggable, x, y, icon, text, new FixColor(TempColor.getBackgroundColor().darker().darker()), scale, alpha, iconColor);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, float scale, double alpha, FixColor iconColor, String string2Circle) {
        drawModernHeader(matrixStack, draggable, x, y, icon, text, new FixColor(TempColor.getBackgroundColor().darker().darker()), scale, alpha, iconColor, string2Circle);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, float scale, double alpha,
                                 FixColor iconColor, String string2Circle, ITextComponent iTextComponent) {
        drawModernHeader(matrixStack, draggable, x, y, icon, text, new FixColor(TempColor.getBackgroundColor().darker().darker()), scale, alpha, iconColor, string2Circle, iTextComponent);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, String text2) {
        drawModernHeader(matrixStack, draggable, x, y, icon, text, text2, new FixColor(TempColor.getBackgroundColor().darker().darker()));
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, FixColor backgroundColor) {
        drawModernHeader(matrixStack, draggable, x, y, icon, text, backgroundColor, 17f, 8f);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, FixColor backgroundColor, double alpha) {
        drawModernHeader(matrixStack, draggable, x, y, icon, text, backgroundColor, 17f, 8f, alpha);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, FixColor backgroundColor, float scale, double alpha) {
        drawModernHeader(matrixStack, draggable, x, y, icon, text, backgroundColor, 17f, 8f, scale, alpha);
    }


    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, FixColor backgroundColor, float scale, double alpha, String circle2String) {
        drawModernHeader(matrixStack, draggable, x, y, icon, text, backgroundColor, 17f, 8f, scale, alpha, circle2String);
    }


    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, FixColor backgroundColor, float scale, double alpha, FixColor iconColor) {
        drawModernHeader(matrixStack, draggable, x, y, icon, text, backgroundColor, 17f, 8f, scale, alpha, iconColor);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, FixColor backgroundColor, float scale, double alpha, FixColor iconColor, String circle2String) {
        drawModernHeader(matrixStack, draggable, x, y, icon, text, backgroundColor, 17f, 8f, scale, alpha, iconColor, circle2String);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, FixColor backgroundColor,
                                 float scale, double alpha, FixColor iconColor, String circle2String, ITextComponent iTextComponent) {
        drawModernHeader(matrixStack, draggable, x, y, icon, text, backgroundColor, 17f, 8f, scale, alpha, iconColor, circle2String, iTextComponent);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, String text2, FixColor backgroundColor) {
        drawModernHeader(matrixStack, draggable, x, y, icon, text, text2, backgroundColor, 17f, 8f);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, String text2, FixColor backgroundColor, FixColor iconColor) {
        drawModernHeader(matrixStack, draggable, x, y, icon, text, text2, backgroundColor, 17f, 8f, iconColor);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, FixColor backgroundColor, float height, float radius) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, null, text, backgroundColor, height, radius, 1.0f);
    }


    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, FixColor backgroundColor,
                                 float height, float radius, double alpha) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, null, text, backgroundColor, height, radius, 1.0f, alpha);
    }



    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, FixColor backgroundColor,
                                 float height, float radius, float scale, double alpha) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, null, text, backgroundColor, height, radius, scale, alpha);
    }


    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, FixColor backgroundColor,
                                 float height, float radius, float scale, double alpha, String circle2String) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, null, text, backgroundColor, height, radius, scale, alpha, circle2String);
    }


    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, FixColor backgroundColor,
                                 float height, float radius, float scale, double alpha, FixColor iconColor) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, null, text, backgroundColor, height, radius, scale, alpha, iconColor);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, FixColor backgroundColor,
                                 float height, float radius, float scale, double alpha, FixColor iconColor, String circle2String) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, null, text, backgroundColor, height, radius, scale, alpha, iconColor, circle2String);
    }


    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, FixColor backgroundColor,
                                 float height, float radius, float scale, double alpha, FixColor iconColor, String circle2String, ITextComponent iTextComponent) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, null, text, backgroundColor, height, radius, scale, alpha, iconColor, circle2String, iTextComponent);
    }


    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, String text2, FixColor backgroundColor, float height, float radius) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, null, text, text2, backgroundColor, height, radius, 1.0f);
    }
    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, String text2, FixColor backgroundColor, float height, float radius, FixColor iconColor) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, null, text, text2, backgroundColor, height, radius, 1.0f, iconColor);
    }

    // Новые методы с ItemStack
    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, ItemStack itemStack, String text) {
        drawModernHeader(matrixStack, draggable, x, y, itemStack, text, new FixColor(TempColor.getBackgroundColor().darker().darker()));
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, ItemStack itemStack, String text, float scale) {
        drawModernHeader(matrixStack, draggable, x, y, itemStack, text, new FixColor(TempColor.getBackgroundColor().darker().darker()), 17,
                8F, scale);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, ItemStack itemStack, String text, float scale, float progress) {
        drawModernHeader(matrixStack, draggable, x, y, itemStack, text, new FixColor(TempColor.getBackgroundColor().darker().darker()), 17,
                8F, scale, progress);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, TextureAtlasSprite itemStack, String text, float scale, float progress) {
        drawModernHeader(matrixStack, draggable, x, y, itemStack, text, new FixColor(TempColor.getBackgroundColor().darker().darker()), 17,
                8F, scale, progress);
    }


    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, ItemStack itemStack, String text, FixColor backgroundColor) {
        drawModernHeader(matrixStack, draggable, x, y, itemStack, text, backgroundColor, 17f, 8f);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, ItemStack itemStack, String text, FixColor backgroundColor, float height, float radius) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, null, itemStack, text, backgroundColor, height, radius, 1.0f);
    }

    // Методы с параметром scale
    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, String text, FixColor backgroundColor, float height, float radius, float scale) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, null, text, backgroundColor, height, radius, scale);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, ItemStack itemStack, String text, FixColor backgroundColor, float height, float radius, float scale) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, null, itemStack, text, backgroundColor, height, radius, scale);
    }
    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, ItemStack itemStack, String text, FixColor backgroundColor,
                                 float height, float radius, float scale, float progress) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, null, itemStack, text, null, backgroundColor, height, radius, scale, progress);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, TextureAtlasSprite itemStack, String text, FixColor backgroundColor,
                                 float height, float radius, float scale, float progress) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, null, null, itemStack, text, null, backgroundColor, height, radius, scale, progress);
    }

    public void drawModernHeader(MatrixStack matrixStack, Draggable draggable, float x, float y, String icon, ItemStack itemStack, String text, FixColor backgroundColor, float height, float radius, float scale) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, text, backgroundColor, height, radius, scale);
    }

    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, String text, FixColor backgroundColor, float height, float radius, float scale) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, text, null, backgroundColor, height, radius, scale, 0);
    }

    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, String text, FixColor backgroundColor, float height, float radius, float scale, double alpha) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, text, null, backgroundColor, height, radius, scale, 0, alpha);
    }

    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, String text, FixColor backgroundColor, float height, float radius, float scale, double alpha, String circle2String) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, text, null, backgroundColor, height, radius, scale, 0, alpha, circle2String);
    }

    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, String text, FixColor backgroundColor, float height, float radius, float scale, double alpha, FixColor iconColor) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, text, null, backgroundColor, height, radius, scale, 0, alpha, iconColor);
    }

    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, String text, FixColor backgroundColor, float height, float radius, float scale, double alpha, FixColor iconColor, String circle2String) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, text, null, backgroundColor, height, radius, scale, 0, alpha, iconColor, circle2String);
    }


    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, String text, FixColor backgroundColor, float height, float radius,
                                          float scale, double alpha, FixColor iconColor, String circle2String, ITextComponent iTextComponent) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, text, null, backgroundColor, height, radius, scale, 0, alpha, iconColor, circle2String, iTextComponent);
    }


    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, String text, String text2, FixColor backgroundColor, float height, float radius, float scale) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, text, text2, backgroundColor, height, radius, scale, 0);
    }

    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, String text, String text2, FixColor backgroundColor, float height, float radius, float scale, FixColor iconColor) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, text, text2, backgroundColor, height, radius, scale, 0, iconColor);
    }

    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, String text, String text2, FixColor backgroundColor, float height, float radius, float scale, float progress) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, null, text, text2, backgroundColor, height, radius, scale, progress);
    }


    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, String text, String text2, FixColor backgroundColor, float height, float radius, float scale, float progress, FixColor iconColor) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, null, text, text2, backgroundColor, height, radius, scale, progress, iconColor);
    }


    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, String text, String text2, FixColor backgroundColor, float height, float radius,
                                          float scale, float progress, double alpha) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, null, text, text2, backgroundColor, height, radius, scale, progress, alpha);
    }
    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, String text, String text2, FixColor backgroundColor, float height, float radius,
                                          float scale, float progress, double alpha, String circle2String) {
        drawModernHeaderInternal2(matrixStack, draggable, x, y, icon, itemStack, null, text, text2, backgroundColor, height, radius, scale, progress, alpha, circle2String);
    }

    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, String text, String text2, FixColor backgroundColor, float height, float radius,
                                          float scale, float progress, double alpha, FixColor iconColor) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, null, text, text2, backgroundColor, height, radius, scale, progress, alpha, iconColor);
    }

    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, String text, String text2, FixColor backgroundColor, float height, float radius,
                                          float scale, float progress, double alpha, FixColor iconColor, String circle2String) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, null, text, text2, backgroundColor, height, radius, scale, progress, alpha, iconColor, circle2String);
    }

    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, String text, String text2, FixColor backgroundColor, float height, float radius,
                                          float scale, float progress, double alpha, FixColor iconColor, String circle2String, ITextComponent iTextComponent) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, null, text, text2, backgroundColor, height, radius, scale, progress, alpha, iconColor, circle2String, iTextComponent);
    }

    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, TextureAtlasSprite resourceLocation, String text, String text2, FixColor backgroundColor, float height, float radius, float scale, float progress) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, resourceLocation, text, text2, backgroundColor, height, radius, scale, progress, 255);
    }

    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, TextureAtlasSprite resourceLocation, String text, String text2, FixColor backgroundColor, float height, float radius,
                                          float scale, float progress, FixColor iconColor) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, resourceLocation, text, text2, backgroundColor, height, radius, scale, progress, 255, iconColor);
    }

    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, TextureAtlasSprite resourceLocation, String text, String text2, FixColor backgroundColor,
                                          float height, float radius, float scale, float progress, double alpha) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, resourceLocation, text, text2, backgroundColor, height, radius, scale, progress, alpha, null);
    }

    private void drawModernHeaderInternal2(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, TextureAtlasSprite resourceLocation, String text, String text2, FixColor backgroundColor,
                                          float height, float radius, float scale, float progress, double alpha, String s) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, resourceLocation, text, text2, backgroundColor, height, radius, scale, progress, alpha, null, s);
    }

    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, TextureAtlasSprite resourceLocation,
                                          String text, String text2, FixColor backgroundColor,
                                          float height, float radius, float scale, float progress,
                                          double alpha, FixColor iconColor) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, resourceLocation, text, text2, backgroundColor, height, radius, scale, progress, alpha, iconColor, null);
    }

    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, TextureAtlasSprite resourceLocation,
                                          String text, String text2, FixColor backgroundColor,
                                          float height, float radius, float scale, float progress,
                                          double alpha, FixColor iconColor, String circle2String) {
        drawModernHeaderInternal(matrixStack, draggable, x, y, icon, itemStack, resourceLocation, text, text2, backgroundColor, height, radius, scale, progress, alpha, iconColor, circle2String, null);
    }
    private void drawModernHeaderInternal(MatrixStack matrixStack, Draggable draggable, float x, float y,
                                          String icon, ItemStack itemStack, TextureAtlasSprite resourceLocation,
                                          String text, String text2, FixColor backgroundColor,
                                          float height, float radius, float scale, float progress,
                                          double alpha, FixColor iconColor, String circle2String, ITextComponent iTextComponent) {

        if (radius == 8 && Round.FLAT_RENDERER) {
            radius = 10;
        }

        x = (int) x; y = (int) y;

        // Применяем общее масштабирование ко всему хедеру одним блоком для оптимизации
        if (scale != 1.0f) {
            Render.scaleStart(x, y, scale);
        }

        // Все размеры теперь базовые, масштабирование применяется глобально
        float textWidth = Fonts.MEDIUM_12.width(text);
        float secondWidth = text2 == null ? 0 : Fonts.SEMIBOLD_10.width(text2);
        float thirdWith = iTextComponent == null ? 0 : Fonts.MEDIUM_12.width(iTextComponent) + 3;
        float totalWidth = textWidth + 23.5f + secondWidth + thirdWith;

        if (draggable != null) draggable.setWidth(totalWidth * scale);

        backgroundColor = backgroundColor.alpha(alpha);

        // Рисуем основной фон
        Round.draw(matrixStack, new Rect(x, y, totalWidth, height), radius, backgroundColor);
        Outline.draw(matrixStack, new Rect(x, y, totalWidth, height), radius, 1, new FixColor(backgroundColor.alpha(36).brighter()));

        // Параметры для круглой иконки
        float circleSize = 14f;
        float circleX = x + 1.5f;
        float circleY = y + 1.5f;

        // Рендерим кружок с иконкой
        renderIconCircle(matrixStack, circleX, circleY, circleSize, backgroundColor, progress, alpha);

        if (iTextComponent != null) {
            Fonts.MEDIUM_12.drawGradient(matrixStack, iTextComponent, x + 18.5f, y + 4.5f, TempColor.getFontColor().alpha(alpha).getRGB());
            Fonts.MEDIUM_12.draw(matrixStack, text, x + 18.5f + thirdWith, y + 4.5f, TempColor.getFontColor().alpha(alpha).getRGB());
            if (text2 != null) {
                Fonts.SEMIBOLD_10.draw(matrixStack, text2, x + 18f + textWidth, y + 5.5f, FixColor.GRAY.getRGB());
            }
        } else {
            Fonts.MEDIUM_12.draw(matrixStack, text, x + 18.5f, y + 4.5f, TempColor.getFontColor().alpha(alpha).getRGB());
            if (text2 != null) {
                Fonts.SEMIBOLD_10.draw(matrixStack, text2, x + 18f + textWidth, y + 5.5f, FixColor.GRAY.getRGB());
            }
        }

        if (circle2String != null) {
            float textWidthC = Fonts.MEDIUM_12.width(circle2String);
            float width = Math.round(Math.max(textWidthC + 12.5f, 18));

            float textHeight = Fonts.MEDIUM_12.height();

            Round.draw(matrixStack, new Rect(x + totalWidth + 1.5f, y, width, height), radius, backgroundColor);
            Outline.draw(matrixStack, new Rect(x + totalWidth + 1.5f, y, width, height), radius, 1, new FixColor(backgroundColor.alpha(36).brighter()));

            float textX = x + totalWidth + 1.5f + (width - textWidthC) / 2f;
            float textY = y + (height - textHeight) / 2f - 2 / 2f;

            Fonts.MEDIUM_12.draw(matrixStack, circle2String, textX, textY, TempColor.getFontColor().alpha(alpha).getRGB());
        }

        // Рисуем иконку или ItemStack
        if (resourceLocation != null || itemStack != null && !itemStack.isEmpty()) {
            // Рендерим ItemStack в центре кружка
            try {
                // Завершаем общее масштабирование
                if (scale != 1.0f) {
                    Render.scaleEnd();
                }

                // Рассчитываем центр кружка в экранных координатах (с учетом общего масштаба)
                float scaledCenterX = x + (1.5f + circleSize/2f) * scale;
                float scaledCenterY = y + (1.5f + circleSize/2f) * scale;

                // Масштаб предмета (относительно дефолтного размера)
                float itemScale = scale * 0.5f;

                // Применяем масштабирование предмета вокруг его центра
                Render.scaleStart(scaledCenterX, scaledCenterY, itemScale);

                // Рендерим предмет с позицией, соответствующей центру минус половина дефолтного размера (16x16)
                if (itemStack == null) {
                    mc.getTextureManager().bindTexture(resourceLocation.getAtlasTexture().getTextureLocation());
                    AbstractGui.blit(matrixStack, (int) (scaledCenterX - 7), (int) (scaledCenterY - 7), 0, 15, 15, resourceLocation);
                } else {
                    mc.getItemRenderer().renderItemIntoGUI(itemStack, (int) (scaledCenterX - 8), (int) (scaledCenterY - 8));
                }

                // Завершаем масштабирование предмета
                Render.scaleEnd();

                // Возвращаем общее масштабирование
                if (scale != 1.0f) {
                    Render.scaleStart(x, y, scale);
                }

            } catch (Exception e) {
                renderFallbackIcon(matrixStack, circleX + circleSize/2f - Fonts.DREAMCORE_12.width("?") / 2f, circleY + 3.5f, alpha);
            }

        } else if (icon != null && !icon.isEmpty()) {
            CFontRenderer cFontRenderer = !icon.contains("ignored") ? Fonts.DREAMCORE_12 : Fonts.SEMIBOLD_15;
            FixColor colorIcon = iconColor == null ? FixColor.GRAY : iconColor;
            if (icon.contains("ignored")) {
                String displayText = icon.replace("ignored", "");
                float iconWidth = cFontRenderer.width(displayText);
                float iconX = circleX + (circleSize - iconWidth) / 2f;
                float iconY = circleY + (circleSize - cFontRenderer.height()) / 2f - 1;

                cFontRenderer.draw(matrixStack, displayText, iconX, iconY, colorIcon.alpha(alpha).getRGB());
            } else {
                float iconWidth = cFontRenderer.width(icon);
                float iconX = circleX + (circleSize - iconWidth) / 2f;
                float iconY = circleY + 3.5f;

                cFontRenderer.draw(matrixStack, icon, iconX, iconY, colorIcon.alpha(alpha).getRGB());
            }
        } else {
            // Рендерим дефолтную иконку если ничего не передано
            renderFallbackIcon(matrixStack, circleX + circleSize/2f - Fonts.DREAMCORE_12.width("?") / 2f, circleY + 3.5f, alpha);
        }

        // Завершаем общее масштабирование
        if (scale != 1.0f) {
            Render.scaleEnd();
        }
    }
    private void renderIconCircle(MatrixStack matrixStack, float x, float y, float circleSize,
                                  FixColor backgroundColor, float progress, double alpha) {
        float circleRadius = circleSize / 2f;
        FixColor circleColor = new FixColor(backgroundColor.alpha(125).brighter().brighter());
        FixColor progressColor = FixColor.WHITE.alpha(alpha).alpha(36);

        // Рисуем фон кружка
        Round.draw(matrixStack, new Rect(x, y, circleSize + 0.5f, circleSize), circleRadius, circleColor);
        Outline.draw(matrixStack, new Rect(x, y, circleSize + 0.5f, circleSize), circleRadius, 1, circleColor.alpha(36));

        // Рисуем прогресс если есть
        if (progress > 0) {
            CircularProgressBar.draw(matrixStack, new Rect(x, y, circleSize + 0.5f, circleSize), progress, 1, progressColor);
        }
    }

    private void renderFallbackIcon(MatrixStack matrixStack, float x, float y, double alpha) {
        Fonts.DREAMCORE_12.draw(matrixStack, "?", x, y, FixColor.GRAY.alpha(alpha).alpha(150).getRGB());
    }
}