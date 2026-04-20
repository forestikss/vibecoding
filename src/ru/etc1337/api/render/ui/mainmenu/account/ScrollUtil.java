package ru.etc1337.api.render.ui.mainmenu.account;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.vector.Vector2f;
import ru.etc1337.api.animations.simple.Animation;
import ru.etc1337.api.animations.simple.Easing;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.render.Render;

@Getter
@Setter
public class ScrollUtil implements QuickImports {
    private float targetScrollPosition; // Renamed 'target' for clarity
    private float maxScrollOffset;      // Renamed 'max' for clarity
    private float currentScrollPosition; // Renamed 'wheel' to better reflect its purpose as the animated scroll value
    private boolean enabled;
    private boolean autoResetWheel; // Renamed 'autoReset' for clarity
    private float scrollSpeed = 5F; // Renamed 'speed' for clarity
    private final Animation animation = new Animation(Easing.SINE_IN_OUT, 150);

    public ScrollUtil() {
        this.enabled = true;
        this.autoResetWheel = true;
        this.targetScrollPosition = 0;
        this.maxScrollOffset = 0;
        this.currentScrollPosition = 0;
    }

    public void update() {
        if (enabled) {
            double mouseWheelDelta = mc.mouseHelper.getWheel();
            if (mouseWheelDelta != 0) {
                targetScrollPosition = (float) Math.min(Math.max(targetScrollPosition + mouseWheelDelta * scrollSpeed, maxScrollOffset), 0);
                // The original code had a 'stretch' variable that seems to be related to over-scrolling.
                // If over-scrolling with a bounce-back effect is desired, it should be re-implemented
                // explicitly. For now, clamping within [maxScrollOffset, 0] is more standard.
            }
        }
        if (autoResetWheel) {
            resetMouseWheelDelta();
        }
        animation.update(targetScrollPosition);
        currentScrollPosition = (float) animation.getValue();
    }

    /**
     * Renders a vertical scrollbar.
     * @param matrixStack The MatrixStack for rendering.
     * @param position The top-left position of the scrollbar.
     * @param visibleHeight The maximum height of the scrollable area.
     * @param alpha The alpha transparency of the scrollbar.
     */
    public void renderV(MatrixStack matrixStack, Vector2f position, float visibleHeight, float alpha) {
        // Only render scrollbar if content is actually scrollable
        if (maxScrollOffset >= 0) return; // No content to scroll or content fits

        float scrollableRange = Math.abs(maxScrollOffset); // The total range the content can scroll
        float percentageScrolled = (currentScrollPosition / maxScrollOffset); // Inverted as currentScrollPosition is negative

        // Calculate bar height based on the ratio of visible content to total content
        // If contentHeight is the total height of all items,
        // then barHeight = (visibleHeight / contentHeight) * visibleHeight
        // Here, maxScrollOffset is negative, representing -(contentHeight - visibleHeight)
        float contentHeight = visibleHeight - maxScrollOffset; // total actual content height
        float barHeight = (visibleHeight / contentHeight) * visibleHeight;

        // Ensure barHeight is not larger than visibleHeight and has a minimum size
        barHeight = Math.max(5.0f, Math.min(barHeight, visibleHeight));


        float scrollY = position.y + (visibleHeight - barHeight) * percentageScrolled;

        Render.drawRect(matrixStack, position.x, scrollY, 0.5F, barHeight, FixColor.WHITE.alpha(alpha));
    }

    /**
     * Renders a horizontal scrollbar.
     * @param matrixStack The MatrixStack for rendering.
     * @param position The top-left position of the scrollbar.
     * @param visibleWidth The maximum width of the scrollable area.
     * @param alpha The alpha transparency of the scrollbar.
     */
    public void renderH(MatrixStack matrixStack, Vector2f position, float visibleWidth, float alpha) {
        // Only render scrollbar if content is actually scrollable
        if (maxScrollOffset >= 0) return; // No content to scroll or content fits

        float scrollableRange = Math.abs(maxScrollOffset);
        float percentageScrolled = (currentScrollPosition / maxScrollOffset);

        float contentWidth = visibleWidth - maxScrollOffset;
        float barWidth = (visibleWidth / contentWidth) * visibleWidth;
        barWidth = Math.max(5.0f, Math.min(barWidth, visibleWidth));

        float scrollX = position.x + (visibleWidth - barWidth) * percentageScrolled;

        Render.drawRect(matrixStack, scrollX, position.y, barWidth, 0.5F, FixColor.WHITE.alpha(alpha));
    }


    /**
     * Resets the scroll position and animation to the initial state (0).
     */
    public void reset() {
        this.animation.setValue(this.currentScrollPosition = this.targetScrollPosition = 0F);
        mc.mouseHelper.setWheel(0F); // Ensure the mouse wheel delta is reset immediately
    }

    /**
     * Resets the Minecraft mouse wheel delta to prevent continuous scrolling.
     */
    private void resetMouseWheelDelta() {
        mc.mouseHelper.setWheel(0F);
    }

    /**
     * Sets the maximum scroll offset. This value should be negative or zero.
     * A negative value indicates that the content is larger than the visible area.
     * @param contentTotalSize The total size of the scrollable content (e.g., sum of heights of all items).
     * @param visibleAreaSize The size of the visible area (e.g., height of the panel).
     */
    public void setMax(float contentTotalSize, float visibleAreaSize) {
        // Calculate maxScrollOffset as a negative value if contentTotalSize > visibleAreaSize
        // This means the scroll position can go from 0 (top/left) down to -(contentTotalSize - visibleAreaSize)
        this.maxScrollOffset = Math.min(0, -(contentTotalSize - visibleAreaSize));
        // Clamp targetScrollPosition to ensure it's within the new valid range
        this.targetScrollPosition = Math.min(0, Math.max(this.targetScrollPosition, this.maxScrollOffset));
    }

    // Getter for currentScrollPosition (wheel)
    public float getWheel() {
        return currentScrollPosition;
    }

    // Setter for targetScrollPosition (target)
    public void setTarget(float target) {
        this.targetScrollPosition = target;
    }

    // Getter for maxScrollOffset (max)
    public float getMax() {
        return maxScrollOffset;
    }
}