package ru.etc1337.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.Client;
import ru.etc1337.api.animations.advanced.Animation;
import ru.etc1337.api.animations.advanced.Easing;
import ru.etc1337.api.animations.advanced.InfinityAnimation;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.render.EventRender3D;
import ru.etc1337.api.render.GhostRenderer3D;
import ru.etc1337.api.render.Render;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.combat.KillAura;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

@ModuleInfo(name = "Target ESP", description = "Отрисовывает разный рендер на игроках, которые бьет Attack Aura", category = ModuleCategory.RENDER)
public class TargetESP extends Module {

    private static final int MAX_PARTICLES = 3;
    private static final float BASE_SIZE = 0.5f;
    private static final float BASE_MUL = 0.05f;
    private static final float ALPHA_STEP = 0.005f;

    private final List<GhostRenderer3D> particles = new ArrayList<>();
    private final Animation targetEspAnim = new Animation().setEasing(Easing.TARGETESP_EASE_OUT_BACK).setSpeed(300);
    private final InfinityAnimation moving = new InfinityAnimation();

    private LivingEntity prevTarget;

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRender3D eventRender3D) {
            prevTarget = Client.getInstance().getModuleManager().get(KillAura.class).getTarget();
            if (prevTarget != null) {
                renderGhost(eventRender3D);
            }
        }
    }

    private void renderGhost(EventRender3D event) {
        if (particles.size() < MAX_PARTICLES) {
            particles.add(new GhostRenderer3D(prevTarget.getPositionVec(), Vector3d.ZERO, 0.3F));
        }

        final List<GhostRenderer3D> toRemove = new ArrayList<>();
        final float fpsFactor = 500f / max(Minecraft.getDebugFPS(), 5);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        Render.startImageRendering(new ResourceLocation("minecraft", "dreamcore/images/glow.png"));

        for (GhostRenderer3D particle : particles) {
            updateParticlePosition(particle, fpsFactor);
            particle.render(event);

            if (particle.getAlpha() < 1) {
                particle.setAlpha(max(0, particle.getAlpha() - ALPHA_STEP * fpsFactor));
            }

            if (particle.getAlpha() <= 0) toRemove.add(particle);
        }

        Render.finishImageRendering();
        particles.removeAll(toRemove);
    }

    private void updateParticlePosition(GhostRenderer3D particle, float fpsFactor) {
        moving.animate(moving.get() + 20, 55);
        targetEspAnim.setForward(prevTarget.hurtTime > 7);

        final int particleIndex = particles.indexOf(particle);
        final float angleOffset = particleIndex * 360f / MAX_PARTICLES;
        final float currentAngle = moving.get() + angleOffset;
        final double radian = toRadians(currentAngle);

        final float x = (float) sin(radian) * (0.3F - targetEspAnim.get() * 0.3F);
        final float z = (float) cos(radian) * (0.3F - targetEspAnim.get() * 0.3F);

        final Vector3d targetPos = prevTarget.getPositionVec()
                .add(x, 0.2 + prevTarget.getHeight() / 2 * sin(toRadians(moving.get() / (particleIndex + 1f))), z);

        final float mul = BASE_MUL * fpsFactor;
        particle.setMotion(targetPos.subtract(particle.getPosition()).mul(mul, mul, mul));
    }
}