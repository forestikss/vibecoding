package ru.etc1337.client.modules.impl.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.animations.simple.Animation;
import ru.etc1337.api.animations.simple.Easing;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventJump;
import ru.etc1337.api.events.impl.game.EventMotion;
import ru.etc1337.api.events.impl.render.EventRenderPost3D;
import ru.etc1337.api.other.LimitList;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

import java.util.List;

@ModuleInfo(name = "Jump Circle", description = "Отрисовывает при прыжке красивые круги", category = ModuleCategory.RENDER)
public class JumpCircle extends Module {
    private static final Tessellator TESSELLATOR = Tessellator.getInstance();
    private static final BufferBuilder BUILDER = TESSELLATOR.getBuffer();

    private final List<Circles> circles = new LimitList<>(20);
    private final SliderSetting radius = new SliderSetting("Радиус", this, 1.0f, 1.0f, 3.0f, 0.1f);
    private final SliderSetting radiusGlow = new SliderSetting("Радиус свечения", this, 3.0f, 0.1f, 3.0f, 0.5f);
    private final SliderSetting speed = new SliderSetting("Скорость", this, 0.1f, 0.1f, 0.3f, 0.01f);

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRenderPost3D eventRender3D) {
            if (circles.isEmpty()) return;

            setupGL();
            BUILDER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

            for (Circles circle : circles) {
                circle.animation.update(radius.getValue());
                float currentRadius = (float) circle.animation.getValue();
                float alpha = calculateAlpha(currentRadius);
                renderCircles(circle, currentRadius, alpha, radiusGlow.getValue() * 20.0f);
            }

            TESSELLATOR.draw();
            cleanupGL();
        }
        if (event instanceof EventJump eventJump) {
            Vector3d pos = mc.player.getPositionVec();
            circles.add(new Circles((float) pos.x, (float) pos.y + 0.05f, (float) pos.z, (long) (500 / speed.getValue())));
        }
        if (event instanceof EventMotion eventMotion) {
            circles.removeIf(circle -> circle.animation.getValue() > radius.getValue() - 0.05f);
        }
    }

    private void setupGL() {
        GlStateManager.pushMatrix();
        GlStateManager.scaleMatrix();
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture();
        GlStateManager.disableAlphaTest();
        GlStateManager.enableBlend();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    private void cleanupGL() {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableTexture();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    private float calculateAlpha(float aps) {
        float maxAlpha = 255.0f;
        float threshold = aps / 6.0f;
        float alpha = aps > threshold ? maxAlpha - threshold * 2000.0f / radius.getValue() : maxAlpha;
        return Math.max(0, alpha);
    }

    private void renderCircles(Circles circles, float f2, float f3, float f4) {
        String string = /*this.color_mode.ehf*/"static";
        int n2 = /*this.custom_color.sG()*/TempColor.getClientColor().getRGB();
        int n3 = /*string.equals("Static") ? n2 : aax.eh(0.0f)*/n2;
        double d2 = circles.ecP;
        double d3 = circles.ecQ;
        double d4 = circles.ecR;
        if (f3 <= 0.0f) {
            f3 = 0.0f;
        }

        float f5 = (float) Math.PI / 180;
        for (float f6 = 0.0f; f6 < 360.0f; f6 += 8.0f) {
            double d5 = Math.sin((f6 - 8.0f) * ((float) Math.PI / 180));
            double d6 = Math.cos((f6 - 8.0f) * ((float) Math.PI / 180));
            double d7 = Math.sin(f6 * ((float) Math.PI / 180));
            double d8 = Math.cos(f6 * ((float) Math.PI / 180));
            double d9 = f2 - f2 / 100.0f * f4;
      /*      if (string.equals("Client")) {
                n2 = aax.eh(f6);
            }*/
            this.addVertex(f2, (int) f3, n2, n3, d9, d7, d8, d5, d6, d2, d3, d4);
            d9 = f2 + f2 / 100.0f * f4;
            this.addVertex(f2, (int) f3, n2, n3, d9, d7, d8, d5, d6, d2, d3, d4);
            this.addVertex(f2, (int) f3, n2, n3, (double) f2 + ((double) f2 - d9) / 8.0, d7, d8, d5, d6, d2, d3, d4);
            n3 = n2;
        }
    }
    private void addVertex(float f2, int n2, int n3, int n4, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9) {
        BUILDER.pos(d7 + d5 * d2, d8, d9 + d6 * d2).color(n4, 0).endVertex();
        BUILDER.pos(d7 + d5 * (double)f2, d8, d9 + d6 * (double)f2).color(n4, n2).endVertex();
        BUILDER.pos(d7 + d3 * (double)f2, d8, d9 + d4 * (double)f2).color(n3, n2).endVertex();
        BUILDER.pos(d7 + d3 * d2, d8, d9 + d4 * d2).color(n3, 0).endVertex();
    }

    @Override
    public void onDisable() {
        circles.clear();
        super.onDisable();
    }

    public static class Circles {
        public final float ecP, ecQ, ecR;
        public final Animation animation;

        public Circles(float x, float y, float z, long duration) {
            this.ecP = x;
            this.ecQ = y;
            this.ecR = z;
            this.animation = new Animation(Easing.SINE_IN_OUT, duration);
            this.animation.setValue(0.0f);
        }
    }
}