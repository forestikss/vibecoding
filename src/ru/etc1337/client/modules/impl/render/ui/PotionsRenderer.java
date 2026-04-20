package ru.etc1337.client.modules.impl.render.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.animations.simple.Animation;
import ru.etc1337.api.animations.simple.Easing;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.fonts.impl.TextBatcher;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.client.modules.impl.render.ui.api.ElementInfo;
import ru.etc1337.client.modules.impl.render.ui.api.Header;
import ru.etc1337.client.modules.impl.render.ui.api.UIElement;

import java.util.ArrayList;
import java.util.List;

@ElementInfo(name = "Potions", icon = "P", initX = 60.0F, initY = 28.0F, initHeight = 17.0F)
public class PotionsRenderer extends UIElement {
    private final BooleanSetting vertical = new BooleanSetting("Vertical", this);

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRender2D eventRender2D) {
            MatrixStack matrixStack = eventRender2D.getMatrixStack();
            float x = getDraggable().getX();
            float y = getDraggable().getY();

            List<EffectInstance> effects = new ArrayList<>(mc.player.getActivePotionEffects());
            if (effects.isEmpty()) return;

            if (vertical.isEnabled()) {
                // Vertical mode
                float yOffset = 0;
                float maxWidth = 0;
                for (EffectInstance effect : effects) {
                    int maxDuration = effect.getDuration();
                    float percentage = (maxDuration * 100f) / 1200; // Assuming 1200 ticks as reference for max duration
                    String text = getDuration(effect);
                    float progress = (maxDuration * 360f) / effect.getDurationMax(); // Progress for visual representation

                    float textWidth = Fonts.MEDIUM_12.width(text);
                    float itemWidth = textWidth + 21.5f;

                    if (itemWidth > maxWidth) {
                        maxWidth = itemWidth;
                    }

                    PotionSpriteUploader potionspriteuploader = mc.getPotionSpriteUploader();
                    TextureAtlasSprite textureatlassprite = potionspriteuploader.getSprite(effect.getPotion());
                    Header.drawModernHeader(matrixStack, getDraggable(), x + 1.5f, y + yOffset, textureatlassprite, text, 0.85f, progress);
                    yOffset += 17; // Height of one element
                }

                getDraggable().setWidth(maxWidth);
                getDraggable().setHeight(yOffset);
            } else {
                // Horizontal mode (original)
                float xOffset = 1.5f;
                for (EffectInstance effect : effects) {
                    int maxDuration = effect.getDuration();
                    float percentage = (maxDuration * 100f) / 1200; // Assuming 1200 ticks as reference for max duration
                    String text = getDuration(effect);
                    float progress = (maxDuration * 360f) / effect.getDurationMax(); // Progress for visual representation

                    float textWidth = Fonts.MEDIUM_12.width(text);
                    float totalWidth = textWidth + 20f;

                    PotionSpriteUploader potionspriteuploader = mc.getPotionSpriteUploader();
                    TextureAtlasSprite textureatlassprite = potionspriteuploader.getSprite(effect.getPotion());
                    Header.drawModernHeader(matrixStack, getDraggable(), x + xOffset + 1.5f, y, textureatlassprite, text, 0.85f, progress);
                    xOffset += totalWidth;
                }

                getDraggable().setWidth(xOffset);
                getDraggable().setHeight(17); // Standard height for horizontal mode
            }
        }
    }

    private String getDuration(EffectInstance effect) {
        if (effect.getIsPotionDurationMax()) {
            return "*:*";
        } else {
            int duration = effect.getDuration();
            int minutes = duration / 1200;
            if (minutes > 30) {
                return "*:*";
            }
            String sec = String.format("%02d", (duration % 1200) / 20);
            return minutes + ":" + sec;
        }
    }

    private String getEffectDisplayString(EffectInstance effect) {
        String displayString = I18n.format(effect.getEffectName());
        int amplifier = effect.getAmplifier() + 1;
        if (amplifier == 1) {
            return displayString;
        } else {
            return displayString + " " + amplifier;
        }
    }
}