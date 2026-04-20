package ru.etc1337.client.modules.impl.render.ui.info;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.render.Render;
import ru.etc1337.client.modules.impl.render.ui.api.ElementInfo;
import ru.etc1337.client.modules.impl.render.ui.api.Header;
import ru.etc1337.client.modules.impl.render.ui.api.UIElement;

@ElementInfo(name = "Totem", initX = 8.0F, initY = 28.0F, initHeight = 17.0F)
public class TotemRenderer extends UIElement {

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRender2D eventRender2D) {
            MatrixStack matrixStack = eventRender2D.getMatrixStack();
            float x = getDraggable().getX();
            float y = getDraggable().getY();

            boolean fake = mc.currentScreen instanceof ChatScreen;
            int count = mc.player.openContainer != null ? (int) mc.player.openContainer.inventorySlots.stream()
                    .filter(slot -> slot.getStack().getItem().equals(Items.TOTEM_OF_UNDYING))
                    .count() : 0;

            if (count == 0 && fake) {
                count = 4;
            }

            if (count == 0) return;

            ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
            Header.drawModernHeader(matrixStack, getDraggable(), x, y, totem, String.valueOf(count), 0.85f);
        }
    }
}