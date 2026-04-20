package ru.etc1337.client.modules.impl.combat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.api.animations.simple.Animation;
import ru.etc1337.api.animations.simple.Easing;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.*;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.game.Maths;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.combat.killaura.TargetFinder;
import ru.etc1337.client.modules.impl.combat.killaura.rotation.api.Rotation;
import ru.kotopushka.compiler.sdk.annotations.Compile;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Aim Bot", description = "Наводится на ентити", category = ModuleCategory.COMBAT)
public class AimBot extends Module {

    private final MultiModeSetting targetType = new MultiModeSetting("Targets", this,
            "Players",
            "Mobs",
            "Animals",
            "Friends");
    private Vector2f rotation;
    private LivingEntity target;
    private final Animation r = new Animation(Easing.SINE_IN_OUT, 150);

    @Compile
    @Override
    public void onEvent(Event event) {
        if (event instanceof EventTick && mc.player != null && mc.world != null) {
            target = updateTarget();
        }
        if (event instanceof EventRender2D eventRender2D) {
            if (target == null) return;
            Vector3d eyePos = mc.player.getBoundingBox().getCenter();
            Vector3d closestPoint = null;
            float minDistance = Float.MAX_VALUE;

            List<Vector3d> points = getPointsAcrossHitbox(target, 0.1f);
            if (points.isEmpty()) return;

            List<Vector3d> visiblePoints = new ArrayList<>(List.of());
            for (Vector3d point : points) {
                if (!Maths.canSeen(point)) continue;
                visiblePoints.add(point);
            }

            if (!visiblePoints.isEmpty()) points = visiblePoints;

            for (Vector3d point : points) {
                float distanceToPoint = (float) eyePos.squareDistanceTo(point);

                if (distanceToPoint < minDistance) {
                    minDistance = distanceToPoint;
                    closestPoint = point;
                }

                Vector2f screenPos = Render.project(point.x, point.y, point.z);
                if (screenPos.x == Float.MAX_VALUE || screenPos.y == Float.MAX_VALUE) continue;

                if (point == closestPoint) {
                    float stackSize = 8;
                    float x = screenPos.x - (stackSize / 2F);
                    float y = screenPos.y - (stackSize / 2F);
                    Render.drawRect(eventRender2D.getMatrixStack(), x, y, stackSize, stackSize, FixColor.GREEN.alpha(255));
                }
            }

            if (closestPoint != null) {
                rotation = Player.get(closestPoint);
                Vector3d pos = closestPoint.subtract(mc.player.getEyePosition(1.0F)).normalize();

                float shortestYawPath = (float) ((((((Math.toDegrees(Math.atan2(pos.z, pos.x)) - 90) - mc.player.rotationYaw) % 360) + 540) % 360) - 180);

                float findPitch = (float) Math.min(90, -Math.toDegrees(Math.atan2(pos.y, Math.hypot(pos.x, pos.z))));

                float targetYaw = mc.player.rotationYaw + shortestYawPath;
                float targetPitch = MathHelper.clamp(findPitch, -90, 90);

                Vector2f correctedRotation = Rotation.correctRotation(
                        targetYaw,
                        targetPitch
                );
                if (!Float.isNaN(correctedRotation.x) && !Float.isNaN(correctedRotation.y)) {
                    this.rotation = new Vector2f(correctedRotation.x, correctedRotation.y);
                }
            }
        }
        if (this.rotation == null || this.target == null) return;
        if (event instanceof EventLook eventLook) {

            float accel1 = mc.pointedEntity == null ? 0.7f
                    : 0f;
            r.update(accel1);
            float accel = r.getValue();
            float yawInterp = Maths.interpolate(mc.player.prevRotationYaw, rotation.x, mc.getTimer().renderPartialTicks * accel);
            float pitchIterp = Maths.interpolate(mc.player.prevRotationPitch, rotation.y, mc.getTimer().renderPartialTicks * accel);

            mc.player.rotationYaw = yawInterp;
            mc.player.rotationPitch = pitchIterp;
        }
        this.rotation = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
    }

    @Compile
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
                        if (x <= bb.minX || x >= bb.maxX || y <= bb.minY || y >= bb.maxY || z <= bb.minZ || z >= bb.maxZ)
                            continue;
                        Vector3d pos = new Vector3d(x, y, z);
                        positions.add(pos);
                    }
                }
            }
        }

        return positions;
    }

    @Compile
    private LivingEntity updateTarget() {
        TargetFinder.EntityFilter filter = new TargetFinder.EntityFilter(targetType);
        if (mc.world != null) TargetFinder.searchTargets(mc.world.getAllEntities(), 3.3f);
        TargetFinder.validateTarget(filter::isValid);
        return TargetFinder.currentTarget;
    }

    @Compile
    @Override
    public void onEnable() {

        super.onEnable();
    }

    @Compile
    @Override
    public void onDisable() {
        TargetFinder.releaseTarget();
        target = null;

        super.onDisable();
    }

}