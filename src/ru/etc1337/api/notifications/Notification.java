package ru.etc1337.api.notifications;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.draggable.Draggable;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.impl.render.ui.api.Header;
import ru.etc1337.api.animations.simple.Animation;
import ru.etc1337.api.animations.simple.Easing;

@Getter
public class Notification implements QuickImports {
    private final String message;
    private final String title;
    private final int lifeTime;
    private final Type type;
    private final Animation fadeAnimation = new Animation(Easing.SINE_IN_OUT, 200);
    private final Timer timer = new Timer();

    private final String[] data;

    private float y;
    private final float width;
    private boolean shouldRemove = false;

    public Notification(@NonNull String title, @NonNull String message, @NonNull Type type, int time) {
        this.lifeTime = time;
        this.type = type;
        this.title = title;
        this.message = message;
        this.data = getStringArray();
        // todo: сделать
        this.width = 15;
        //this.width = Header.getWidth(data[0], data[1], 0.5f)[2];
        this.y = mc.getMainWindow().getScaledHeight() / 2f + 10;
    }

    private String[] getStringArray() {
        String title = type == Type.CUSTOM ? this.title : type.icon;
        String message = this.message;
        return new String[] { title, message };
    }

    public void render(MatrixStack matrix, Draggable draggable,  boolean staticD, float targetY, float targetX) {
        fadeAnimation.update(shouldRemove ? 0 : 1);

        if (fadeAnimation.getValue() > 0.05F) {
            float animatedAlpha = fadeAnimation.getValue() * 255;
            y = animate(y, targetY);
            int x = (int) (draggable != null && !staticD ? draggable.getX() : mc.getMainWindow().getScaledWidth() / 2f - width / 2f);
            FixColor color = type == Type.CUSTOM ? TempColor.getClientColor() : type.color;
// todo: сделать
            // Header.drawHeader(matrix, null, x, y, 0.5f, 12F, color, animatedAlpha, data[0], data[1]);
        }
    }

    public void markForRemoval() {
        shouldRemove = true;
    }

    public boolean shouldDelete() {
        return shouldRemove && fadeAnimation.getValue() <= 0.05F;
    }

    public boolean isFinished() {
        return timer.finished(lifeTime);
    }

    private float animate(float value, float target) {
        return value + (target - value) / 8F; // 8 - скорость анимации
    }

    public enum SoundType {
        NOTIFY,
        MODULE_ENABLE,
        MODULE_DISABLE,
        NONE
    }

    @AllArgsConstructor
    @Getter
    public enum Type {
        SUCCESS("Success", new FixColor(0x00FF00), "H"),
        INFO("Information", new FixColor(0x00FFFF), "J"),
        WARNING("Warning", new FixColor(0xFFD700), "L"),
        ERROR("Error", new FixColor(0xFF0000), "I"),
        CUSTOM(null, null, null);

        private final String name;
        private final FixColor color;
        private final String icon;
    }
}