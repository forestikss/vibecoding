package ru.etc1337.client.modules.impl.render.ui.info;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.client.modules.impl.render.ui.api.ElementInfo;
import ru.etc1337.client.modules.impl.render.ui.api.Header;
import ru.etc1337.client.modules.impl.render.ui.api.UIElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ElementInfo(name = "Armor", initX = 8.0F, initY = 28.0F, initHeight = 17.0F)
public class ArmorRenderer extends UIElement {
    private final BooleanSetting vertical = new BooleanSetting("Vertical", this);

    // ширина одного слота: иконка(17) + текст + отступ
    private static final float SLOT_BASE  = 17f;
    private static final float H          = 17f;
    private static final float PAD        = 3f;
    private static final float RADIUS     = 8f;

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventRender2D eventRender2D)) return;

        MatrixStack matrixStack = eventRender2D.getMatrixStack();
        float x = getDraggable().getX();
        float y = getDraggable().getY();

        List<ItemStack> items = new ArrayList<>();
        for (ItemStack itemStack : mc.player.getArmorInventoryList()) {
            if (!itemStack.isEmpty()) items.add(itemStack);
        }

        boolean fake = mc.currentScreen instanceof ChatScreen;
        if (items.isEmpty() && fake) {
            items.add(Items.DIAMOND_HELMET.getDefaultInstance());
            items.add(Items.DIAMOND_CHESTPLATE.getDefaultInstance());
            items.add(Items.DIAMOND_LEGGINGS.getDefaultInstance());
            items.add(Items.DIAMOND_BOOTS.getDefaultInstance());
        }

        if (items.isEmpty()) return;
        Collections.reverse(items);

        if (vertical.isEnabled()) {
            renderVertical(matrixStack, x, y, items);
        } else {
            renderHorizontal(matrixStack, x, y, items);
        }
    }

    private void renderHorizontal(MatrixStack matrixStack, float x, float y, List<ItemStack> items) {
        FixColor bg = new FixColor(TempColor.getBackgroundColor().darker().darker());

        // считаем суммарную ширину
        float totalWidth = PAD;
        for (ItemStack item : items) {
            String text = getDurabilityText(item);
            totalWidth += SLOT_BASE + Fonts.MEDIUM_12.width(text) + PAD;
        }

        // один общий фон
        Round.draw(matrixStack, new Rect(x, y, totalWidth, H), RADIUS, bg);

        // рисуем каждый слот без своего фона (прозрачный bg)
        float xOffset = PAD;
        for (ItemStack item : items) {
            String text = getDurabilityText(item);
            float progress = getDurabilityProgress(item);
            float slotWidth = SLOT_BASE + Fonts.MEDIUM_12.width(text);

            Header.drawModernHeader(matrixStack, null, x + xOffset, y, item, text,
                    FixColor.BLACK.alpha(0), H, RADIUS, 0.85f, progress);

            xOffset += slotWidth + PAD;
        }

        getDraggable().setWidth(totalWidth);
        getDraggable().setHeight(H);
    }

    private void renderVertical(MatrixStack matrixStack, float x, float y, List<ItemStack> items) {
        FixColor bg = new FixColor(TempColor.getBackgroundColor().darker().darker());

        float maxWidth = 0;
        for (ItemStack item : items) {
            float w = SLOT_BASE + Fonts.MEDIUM_12.width(getDurabilityText(item)) + PAD * 2;
            if (w > maxWidth) maxWidth = w;
        }
        float totalHeight = H * items.size();

        // один общий фон
        Round.draw(matrixStack, new Rect(x, y, maxWidth, totalHeight), RADIUS, bg);

        float yOffset = 0;
        for (ItemStack item : items) {
            String text = getDurabilityText(item);
            float progress = getDurabilityProgress(item);

            Header.drawModernHeader(matrixStack, null, x + PAD, y + yOffset, item, text,
                    FixColor.BLACK.alpha(0), H, RADIUS, 0.85f, progress);

            yOffset += H;
        }

        getDraggable().setWidth(maxWidth);
        getDraggable().setHeight(totalHeight);
    }

    private String getDurabilityText(ItemStack item) {
        int maxDamage = item.getMaxDamage();
        if (maxDamage == 0) return "100%";
        int durability = maxDamage - item.getDamage();
        return String.format("%d%%", (int) ((durability * 100f) / maxDamage));
    }

    private float getDurabilityProgress(ItemStack item) {
        int maxDamage = item.getMaxDamage();
        if (maxDamage == 0) return 360f;
        int durability = maxDamage - item.getDamage();
        return (durability * 360f) / maxDamage;
    }
}
