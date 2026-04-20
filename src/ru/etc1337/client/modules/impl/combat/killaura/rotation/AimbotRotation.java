package ru.etc1337.client.modules.impl.combat.killaura.rotation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.Client;
import ru.etc1337.api.animations.simple.Animation;
import ru.etc1337.api.animations.simple.Easing;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventAttack;
import ru.etc1337.api.events.impl.game.EventLook;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.game.Maths;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.game.RayTrace;
import ru.etc1337.api.render.Render;
import ru.etc1337.client.modules.impl.combat.KillAura;
import ru.etc1337.client.modules.impl.combat.killaura.rotation.api.Rotation;

import java.util.ArrayList;
import java.util.List;

public class AimbotRotation extends Rotation {


    @Override
    public void onEvent(Event event) {
        KillAura killAura = Client.getInstance().getModuleManager().get(KillAura.class);
        LivingEntity target = killAura.getTarget();

        if (target == null) return;
        if (event instanceof EventRender2D eventRender2D) {
            Vector3d pos = target.getPositionVec().subtract(mc.player.getEyePosition(mc.getTimer().renderPartialTicks)).normalize();

            float shortestYawPath = (float) ((((((Math.toDegrees(Math.atan2(pos.z, pos.x)) - 90) - mc.player.rotationYaw) % 360) + 540) % 360) - 180);

            float findPitch = (float) Math.min(90, -Math.toDegrees(Math.atan2(pos.y, Math.hypot(pos.x, pos.z))));

            double val = killAura.yawRandom.isEnabled() ? Maths.randomSimple(-1, 1) : 0;
            float targetYaw = (float) (mc.player.rotationYaw + shortestYawPath + val);
            float targetPitch = (float) (MathHelper.clamp(findPitch, -90, 90));

            Vector2f correctedRotation = Rotation.correctRotation(
                    targetYaw,
                    targetPitch
            );
            if (!Float.isNaN(correctedRotation.x) && !Float.isNaN(correctedRotation.y)) {
                this.rotation = new Vector2f(correctedRotation.x, correctedRotation.y);
            }
        }
        if (this.rotation == null) return;
        if (event instanceof EventLook eventLook) {
            float accel = (float) killAura.universalSpeedX.getValue();
            float yawInterp = Maths.interpolate(mc.player.prevRotationYaw, rotation.x, mc.getTimer().renderPartialTicks * accel);

            mc.player.rotationYaw = yawInterp;

            eventLook.setYaw(0);
        }

        super.onEvent(event);
    }

    @Override
    public void update(LivingEntity target) {

    }


    public List<Vector3d> getPointsAcrossHitbox(LivingEntity livingEntity, double step) {
        List<Vector3d> positions = new ArrayList<>();
        if (livingEntity == null) {
            return positions;
        }

        AxisAlignedBB bb = livingEntity.getBoundingBox();
        double centerX = (bb.minX + bb.maxX) / 2.0;
        double centerY = (bb.minY + bb.maxY) / 2.0;
        double centerZ = (bb.minZ + bb.maxZ) / 2.0;

        for (double radius = 0; radius <= Math.max(bb.maxX - bb.minX, Math.max(bb.maxY - bb.minY, bb.maxZ - bb.minZ)) / 2.0; radius += step) {
            for (double x = centerX - radius; x <= centerX + radius; x += step) {
                for (double y = centerY - radius; y <= centerY + radius; y += step) {
                    for (double z = centerZ - radius; z <= centerZ + radius; z += step) {
                        if (x <= bb.minX || x >= bb.maxX || y <= bb.minY || y >= bb.maxY || z <= bb.minZ || z >= bb.maxZ) continue;
                        Vector3d pos = new Vector3d(x, y, z);
                        positions.add(pos);
                    }
                }
            }
        }

        return positions;
    }
}