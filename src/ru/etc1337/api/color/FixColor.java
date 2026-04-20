package ru.etc1337.api.color;

import com.mojang.blaze3d.platform.GlStateManager;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.game.Maths;

import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class FixColor extends Color {

    public static final FixColor BLACK = new FixColor(0,0,0);
    public static final FixColor WHITE = new FixColor(255,255,255);
    public static final FixColor CYAN = new FixColor(0, 255, 255);
    public static final FixColor RED = new FixColor(255, 0, 0);
    public static final FixColor BLUE = new FixColor(0, 0, 255);
    public static final FixColor YELLOW = new FixColor(255, 255, 0);
    public static final FixColor GREEN = new FixColor(0, 255, 0);
    public static final FixColor ORANGE = new FixColor(255, 165, 0);
    public static final FixColor GRAY = new FixColor(128, 128, 128);
    public static final FixColor PURPLE = new FixColor(128, 0, 128);
    public static final FixColor LIGHT_PURPLE = new FixColor(200, 160, 255);
    public static final FixColor TRANSPARENT = new FixColor(0,0,0,0);
    private static final ConcurrentHashMap<ColorKey, CacheEntry> colorCache = new ConcurrentHashMap<>();
    private static final DelayQueue<CacheEntry> cleanupQueue = new DelayQueue<>();

    public FixColor(double r, double g, double b, double a) {
        super(fix(r), fix(g), fix(b), fix(a));
    }

    public FixColor(double r, double g, double b) {
        super(fix(r), fix(g), fix(b));
    }

    public FixColor(int hex) {
        super(hex);
    }

    public static int multAlpha(int color, float percent01) {
        return getColor(red(color), green(color), blue(color), Math.round(a(color) * percent01));
    }
    public static int fade(int speed, int index, int first, int second) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = angle >= 180 ? 360 - angle : angle;
        return overCol(first, second, angle / 180f);
    }
    public static int fade(int index) {
        FixColor clientColor = TempColor.getClientColor();
        return fade(10, index, clientColor.getRGB(), clientColor.darker().darker().getRGB());
    }
    public static int overCol(int color1, int color2, float percent01) {
        final float percent = MathHelper.clamp(percent01, 0F, 1F);
        return getColor(
                Maths.lerp(r(color1), r(color2), percent),
                Maths.lerp(g(color1), g(color2), percent),
                Maths.lerp(b(color1), b(color2), percent),
                Maths.lerp(a(color1), a(color2), percent)
        );
    }
    public FixColor alpha(final float alpha) {
        return new FixColor(this.getRed(), this.getGreen(), this.getBlue(), this.getAlpha() * (alpha / 255));
    }
    public static int replAlpha(int color, float alpha) {
        return getColor(red(color), green(color), blue(color), (int) alpha);
    }

    public FixColor withAlphaPercentage(float alphaPercentage) {
        return new FixColor(getRed(), getGreen(), getBlue(), Maths.getPercentage((int) alphaPercentage, 100));
    }
    public FixColor(Color color) {
        super(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    private static int fix(double val) {
        return (int) Math.max(0, Math.min(255, val));
    }
    public static int applyAlpha(int var0, int var1) {
        return MathHelper.clamp(var1, 0, 255) << 24 | var0 & 16777215;
    }
    public static FixColor decodeColor(String nm) {
        Integer intval = Integer.decode(nm);
        int i = intval;
        return new FixColor((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF);
    }
    public static float[] rgb(final int color) {
        return new float[]{
                (color >> 16 & 0xFF) / 255f,
                (color >> 8 & 0xFF) / 255f,
                (color & 0xFF) / 255f,
                (color >> 24 & 0xFF) / 255f
        };
    }

    public static int red(int c) {
        return (c >> 16) & 0xFF;
    }

    public static int green(int c) {
        return (c >> 8) & 0xFF;
    }

    public static int blue(int c) {
        return c & 0xFF;
    }

    private static int computeColor(int red, int green, int blue, int alpha) {
        return ((MathHelper.clamp(alpha, 0, 255) << 24) |
                (MathHelper.clamp(red, 0, 255) << 16) |
                (MathHelper.clamp(green, 0, 255) << 8) |
                MathHelper.clamp(blue, 0, 255));
    }


    public static int getColor(int red, int green, int blue, int alpha) {
        ColorKey key = new ColorKey(red, green, blue, alpha);
        CacheEntry cacheEntry = colorCache.computeIfAbsent(key, k -> {
            CacheEntry newEntry = new CacheEntry(k, computeColor(red, green, blue, alpha), 60000);
            cleanupQueue.offer(newEntry);
            return newEntry;
        });
        return cacheEntry.getColor();
    }

    public static int rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        int color = Color.HSBtoRGB(hue, saturation, brightness);
        return getColor(red(color), green(color), blue(color), Math.round(opacity * 255));
    }

    public FixColor alpha(double alpha) {
        return new FixColor(this.getRed(), this.getGreen(), this.getBlue(), this.getAlpha() * (alpha / 255));
    }
    public static int r(int c) {
        return c >> 16 & 0xFF;
    }

    public static int g(int c) {
        return c >> 8 & 0xFF;
    }

    public static int b(int c) {
        return c & 0xFF;
    }

    public static int a(int c) {
        return c >> 24 & 0xFF;
    }
    public static float[] getRGBAf(int c) {
        return new float[]{(float) r(c) / 255.F, (float) g(c) / 255.F, (float) b(c) / 255.F, (float) a(c) / 255.F};
    }
    public Color getColor() {
        return new Color(getRed(), getGreen(), getBlue(), getAlpha());
    }
    public static float[] getRGBAFloat(FixColor color) {
        return new float[] { color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F };
    }
    public static StringTextComponent gradient(String message, int first, int end) {

        StringTextComponent text = new StringTextComponent("");
        for (int i = 0; i < message.length(); i++) {
            text.append(new StringTextComponent(String.valueOf(message.charAt(i))).setStyle(net.minecraft.util.text.Style.EMPTY.setColor(new net.minecraft.util.text.Color(interpolateColor(first, end, (float) i / message.length())))));
        }

        return text;

    }
    public static void glSetupColor(Color color) {
        GL11.glColor4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
    }
    public static void color(int color, float alpha) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GlStateManager.color4f(r, g, b, alpha);
    }
    public static void color(int color) {
        color(color, (float) (color >> 24 & 255) / 255.0F);
    }
    public static int getRed(final int hex) {
        return hex >> 16 & 255;
    }

    public static int getGreen(final int hex) {
        return hex >> 8 & 255;
    }

    public static int getBlue(final int hex) {
        return hex & 255;
    }

    public static int getAlpha(final int hex) {
        return hex >> 24 & 255;
    }
    public static int interpolateColor(int color1, int color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));

        int red1 = getRed(color1);
        int green1 = getGreen(color1);
        int blue1 = getBlue(color1);
        int alpha1 = getAlpha(color1);

        int red2 = getRed(color2);
        int green2 = getGreen(color2);
        int blue2 = getBlue(color2);
        int alpha2 = getAlpha(color2);

        int interpolatedRed = interpolateInt(red1, red2, amount);
        int interpolatedGreen = interpolateInt(green1, green2, amount);
        int interpolatedBlue = interpolateInt(blue1, blue2, amount);
        int interpolatedAlpha = interpolateInt(alpha1, alpha2, amount);

        return (interpolatedAlpha << 24) | (interpolatedRed << 16) | (interpolatedGreen << 8) | interpolatedBlue;
    }
    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return interpolate(oldValue, newValue, (float) interpolationValue).intValue();
    }
    public static Double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class ColorKey {
        final int red, green, blue, alpha;
    }

    @Getter
    private static class CacheEntry implements Delayed {
        private final ColorKey key;
        private final int color;
        private final long expirationTime;

        CacheEntry(ColorKey key, int color, long ttl) {
            this.key = key;
            this.color = color;
            this.expirationTime = System.currentTimeMillis() + ttl;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long delay = expirationTime - System.currentTimeMillis();
            return unit.convert(delay, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            if (other instanceof CacheEntry) {
                return Long.compare(this.expirationTime, ((CacheEntry) other).expirationTime);
            }
            return 0;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }

    }
}
