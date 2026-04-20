package ru.etc1337.client.modules.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.render.EventRender3D;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.settings.impl.ColorSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "FireFly", description = "Светлячки по всему чанку", category = ModuleCategory.RENDER)
public class FireFly extends Module {

    private static final ResourceLocation GLOW_TEX =
            new ResourceLocation("minecraft", "dreamcore/images/glow.png");

    private final ColorSetting color     = new ColorSetting("Цвет", this, new FixColor(180, 255, 120).getRGB());
    private final SliderSetting count    = new SliderSetting("Количество", this, 20f, 5f, 60f, 1f);
    private final SliderSetting speed    = new SliderSetting("Скорость", this, 1.0f, 0.2f, 3.0f, 0.1f);
    private final SliderSetting range    = new SliderSetting("Радиус чанка", this, 16f, 8f, 48f, 2f);
    private final SliderSetting trailLen = new SliderSetting("Хвост", this, 20f, 5f, 50f, 1f);
    private final SliderSetting bright   = new SliderSetting("Яркость", this, 1.5f, 0.5f, 3.0f, 0.1f);

    private final List<Fly> flies = new ArrayList<>();

    @Override public void onEnable()  { flies.clear(); super.onEnable(); }
    @Override public void onDisable() { flies.clear(); super.onDisable(); }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (mc.player == null || mc.world == null) return;
            int need = (int) count.getValue();
            Vector3d origin = mc.player.getPositionVec();
            float r = range.getValue();
            while (flies.size() < need) flies.add(new Fly(origin, r));
            while (flies.size() > need) flies.remove(flies.size() - 1);
            float spd = speed.getValue();
            for (Fly fly : flies) fly.update(origin, r, spd);
        }

        if (event instanceof EventRender3D e) {
            if (mc.player == null || flies.isEmpty()) return;
            render(e.getMatrixStack());
        }
    }

    private void render(MatrixStack ms) {
        FixColor col = color.getColor();
        float b = bright.getValue();

        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        for (Fly fly : flies) {
            int sz = fly.trail.size();
            if (sz < 2) continue;

            // хвост — затухает от хвоста к голове
            for (int i = 0; i < sz - 1; i++) {
                float t = (float) i / (sz - 1);
                float alpha = t * t * t * 0.85f * b;
                float size  = 0.005f + 0.022f * t;
                drawSprite(ms, fly.trail.get(i), size, col.alpha(Math.min(255, (int)(alpha * 255))));
            }

            // голова — яркая
            Vector3d head = fly.trail.get(sz - 1);
            float headAlpha = Math.min(1f, b);
            // мягкое большое свечение
            drawSprite(ms, head, 0.18f, col.alpha((int)(55 * headAlpha)));
            // среднее свечение
            drawSprite(ms, head, 0.08f, col.alpha((int)(140 * headAlpha)));
            // яркое ядро
            drawSprite(ms, head, 0.035f, col.alpha(Math.min(255, (int)(255 * headAlpha))));
        }

        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.depthMask(true);
        RenderSystem.clearCurrentColor();
    }

    private void drawSprite(MatrixStack ms, Vector3d pos, float sz, FixColor col) {
        ms.push();
        Render.setupOrientationMatrix(ms, pos.x, pos.y, pos.z);
        ms.rotate(mc.getRenderManager().getCameraOrientation());
        ms.push();
        ms.rotate(Vector3f.ZP.rotationDegrees(180f));
        ms.translate(0, -sz, -sz);
        Render.drawImage(ms, GLOW_TEX, -sz, -sz, 0, sz * 2, sz * 2, col);
        ms.pop();
        ms.pop();
    }

    // ---- Светлячок ----
    private class Fly {
        Vector3d pos;
        // целевая точка — случайная в чанке
        Vector3d target;
        final List<Vector3d> trail = new ArrayList<>();
        // время до смены цели
        int ticksToNewTarget = 0;

        Fly(Vector3d origin, float r) {
            pos    = randomPos(origin, r);
            target = randomPos(origin, r);
        }

        private Vector3d randomPos(Vector3d origin, float r) {
            double x = origin.x + (Math.random() - 0.5) * r * 2;
            double z = origin.z + (Math.random() - 0.5) * r * 2;
            // высота: от земли до ~4 блоков — случайно по всему чанку
            double y = origin.y - 1.0 + Math.random() * 5.0;
            return new Vector3d(x, y, z);
        }

        void update(Vector3d origin, float r, float spd) {
            // меняем цель когда достигли или по таймеру
            ticksToNewTarget--;
            double dist = pos.distanceTo(target);
            if (dist < 0.3 || ticksToNewTarget <= 0) {
                target = randomPos(origin, r);
                ticksToNewTarget = 40 + (int)(Math.random() * 60);
            }

            // плавное движение к цели
            double lerpF = 0.025 * spd;
            pos = new Vector3d(
                    pos.x + (target.x - pos.x) * lerpF,
                    pos.y + (target.y - pos.y) * lerpF,
                    pos.z + (target.z - pos.z) * lerpF);

            // лёгкое покачивание по Y
            double wobble = Math.sin(System.currentTimeMillis() * 0.002 + pos.x * 3) * 0.003;
            pos = pos.add(0, wobble, 0);

            trail.add(pos);
            int max = (int) trailLen.getValue();
            while (trail.size() > max) trail.remove(0);
        }
    }
}
