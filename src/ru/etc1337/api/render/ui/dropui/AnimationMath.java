package ru.etc1337.api.render.ui.dropui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class AnimationMath {
    private static float cachedDeltaTime = 1.0f;
    private static int lastFps = -1;

    public static float deltaTime() {
        int currentFps = Minecraft.getDebugFPS();
        if (currentFps != lastFps) {
            cachedDeltaTime = currentFps > 0 ? 1.0f / currentFps : 1.0f;
            lastFps = currentFps;
        }
        return cachedDeltaTime;
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public static float lerp(float end, float start, float multiple) {
        return (float) (end + (start - end) * MathHelper.clamp(AnimationMath.deltaTime() * multiple, 0, 1));
    }

    public static Vector3d fast(Vector3d end, Vector3d start, float multiple) {
        return new Vector3d(
                fast((float) end.getX(), (float) start.getX(), multiple),
                fast((float) end.getY(), (float) start.getY(), multiple),
                fast((float) end.getZ(), (float) start.getZ(), multiple));
    }

    public static float fast(float end, float start, float multiple) {
        float delta = deltaTime() * multiple;
        float clampedDelta = clamp(delta, 0, 1);
        return (1 - clampedDelta) * end + clampedDelta * start;
    }
}