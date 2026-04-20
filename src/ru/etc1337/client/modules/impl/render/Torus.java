package ru.etc1337.client.modules.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;
import ru.etc1337.api.animations.advanced.Animation;
import ru.etc1337.api.animations.advanced.Easing;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventAttack;
import ru.etc1337.api.events.impl.render.EventRender3D;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.shaders.Shader;
import ru.etc1337.api.render.shaders.impl.Glass;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

import java.util.ArrayList;

@ModuleInfo(name = "Torus", description = "Водяной бублик", category = ModuleCategory.RENDER)
public class Torus extends Module {
    public SliderSetting noise = new SliderSetting("Искажение", this, 5, 1, 10, 1);
    public SliderSetting reflect = new SliderSetting("Водянистость", this, 100, 50, 100, 5);
    public SliderSetting blur = new SliderSetting("Размытие", this, 0, 0, 10, 1);
    public SliderSetting sizeSetting = new SliderSetting("Размер", this, 0.5F, 0.5F, 1.5F, 0.1F);
    ArrayList<TorusObj> toruses = new ArrayList<>();


    @Override
    public void onEvent(Event event) {
        if (event instanceof EventAttack e) {
            Vector3d particlePos = e.getTarget().getPositionVec().add(0, e.getTarget().getHeight()/2F, 0);
            TorusObj particle = new TorusObj(particlePos);

            toruses.add(particle);
        }

        if (event instanceof EventRender3D e) {
            toruses.removeIf(torus -> torus.shouldRemove());

            if (toruses.isEmpty()) return;

            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.defaultBlendFunc();

            Glass.draw(FixColor.WHITE, noise.getValue(), reflect.getValue(), blur.getValue() * 5F);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder builder = tessellator.getBuffer();
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            MatrixStack ms = e.getMatrixStack();

            for (TorusObj particle : toruses) {
                particle.render(e.getMatrixStack(), builder);
            }

            tessellator.draw();
            Glass.end();
            RenderSystem.disableBlend();
            RenderSystem.enableTexture();
            Render.resetColor();
        }
    }


    class TorusObj {

        final Vector2f rotation;
        Animation sizing = new Animation().setEasing(Easing.BOTH_CIRC).setSpeed(300);
        Timer timer = new Timer();
        Vector3d pos;
        float size = 1.0F;
        long lifeTime;

        public TorusObj(Vector3d pos) {
            Vector3d sub = mc.player.getEyePosition(1).sub(pos).mul(0.5F);
            this.pos = pos.add(MathHelper.clamp(sub.x, -0.5f, 0.5f), MathHelper.clamp(sub.y, -1, 1), MathHelper.clamp(sub.z, -0.5f, 0.5f));
            rotation = Player.get(pos);
            lifeTime = 1000;
        }

        public void render(MatrixStack matrices, BufferBuilder builder) {
            sizing.setEasing(Easing.EASE_OUT_CIRC);
            sizing.setSpeed((int) lifeTime);
            sizing.setForward(true);

            matrices.push();

            FixColor render = FixColor.WHITE.alpha(1D);

            float size = sizeSetting.getValue();
            float radius = sizing.get()*size;
            float innerRadius = (size-sizing.get()*size) * radius;
            float outerRadius = (sizing.get()) * radius;

            Matrix4f matrix = matrices.getLast().getMatrix();

            Vector3d camera = Render.cameraPos();
            double x = pos.getX() - camera.x;
            double y = pos.getY() - camera.y;
            double z = pos.getZ() - camera.z;

            matrices.translate(x, y, z);

            matrices.rotate(Vector3f.YN.rotation((float) Math.toRadians(rotation.x)));
            matrices.rotate(Vector3f.XN.rotation((float) Math.toRadians(-rotation.y + 90)));

            int segments = 30;

            for (int i = 0; i < segments; i++) {
                double theta1 = 2 * Math.PI * i / segments;
                double theta2 = 2 * Math.PI * (i + 1) / segments;

                for (int j = 0; j < segments; j++) {
                    double phi1 = 2 * Math.PI * j / segments;
                    double phi2 = 2 * Math.PI * (j + 1) / segments;

                    float x1 = (float) ((outerRadius + innerRadius * Math.cos(phi1)) * Math.cos(theta1));
                    float y1 = (float) (innerRadius * Math.sin(phi1));
                    float z1 = (float) ((outerRadius + innerRadius * Math.cos(phi1)) * Math.sin(theta1));

                    float x2 = (float) ((outerRadius + innerRadius * Math.cos(phi2)) * Math.cos(theta1));
                    float y2 = (float) (innerRadius * Math.sin(phi2));
                    float z2 = (float) ((outerRadius + innerRadius * Math.cos(phi2)) * Math.sin(theta1));

                    float x3 = (float) ((outerRadius + innerRadius * Math.cos(phi2)) * Math.cos(theta2));
                    float y3 = (float) (innerRadius * Math.sin(phi2));
                    float z3 = (float) ((outerRadius + innerRadius * Math.cos(phi2)) * Math.sin(theta2));

                    float x4 = (float) ((outerRadius + innerRadius * Math.cos(phi1)) * Math.cos(theta2));
                    float y4 = (float) (innerRadius * Math.sin(phi1));
                    float z4 = (float) ((outerRadius + innerRadius * Math.cos(phi1)) * Math.sin(theta2));

                    builder.pos(matrix, x1, y1, z1).color(render.getRGB()).endVertex();
                    builder.pos(matrix, x2, y2, z2).color(render.getRGB()).endVertex();
                    builder.pos(matrix, x3, y3, z3).color(render.getRGB()).endVertex();
                    builder.pos(matrix, x4, y4, z4).color(render.getRGB()).endVertex();
                }
            }

            matrices.pop();
        }


        public boolean shouldRemove() {
            return timer.passed(lifeTime);
        }

    }
}
