package ru.etc1337.client.modules.impl.combat.killaura.rotation;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.Client;
import ru.etc1337.api.game.Player;
import ru.etc1337.client.modules.impl.combat.ElytraTarget;
import ru.etc1337.client.modules.impl.combat.killaura.rotation.api.Rotation;

import java.util.concurrent.ThreadLocalRandom;

public class ElytraRotation extends Rotation {
    @Getter
    private Vector3d targetVec;
    
    public Vector3d getPoint(LivingEntity target) {
        if (target == null) return Vector3d.ZERO;
        return getBestPoint(mc.player.getEyePosition(mc.getTimer().renderPartialTicks), target);
    }
    
    public Vector3d getBestPoint(Vector3d pos, LivingEntity entity) {
        if (entity == null) return Vector3d.ZERO;
        return new Vector3d(
                MathHelper.clamp(pos.x,
                        entity.getBoundingBox().minX,
                        entity.getBoundingBox().maxX),

                MathHelper.clamp(pos.y,
                        entity.getBoundingBox().minY,
                        entity.getBoundingBox().maxY),

                MathHelper.clamp(pos.z,
                        entity.getBoundingBox().minZ,
                        entity.getBoundingBox().maxZ)
        );
    }

    public Vector3d getTargetVec(final Entity entity, final boolean target) {
        ElytraTarget elytraTarget = Client.getInstance().getModuleManager().get(ElytraTarget.class);
        Vector3d entityPos = entity instanceof LivingEntity targetEntity ? getPoint(targetEntity) : entity.getPositionVec().add(0, entity.getEyeHeight(), 0);
        double scale = elytraTarget.getPrediction(entity);

        if (target && entity instanceof LivingEntity livingTargetEntity &&
                elytraTarget.shouldTarget(livingTargetEntity) && Player.getBps(livingTargetEntity) >= 20F) {
            entityPos = entityPos.add(livingTargetEntity.getMotion().scale(scale));
        }

        this.targetVec = entityPos;
        return entityPos;
    }

    public Vector2f getBoundingBox(final Entity entity) {
        Vector3d playerPos = mc.player.getEyePosition(1.0F);
        Vector3d entityPos = getTargetVec(entity, true);
        return this.calculateDiff(playerPos, entityPos);
    }

    public Vector2f calculateDiff(final Vector3d from, final Vector3d to) {
        Vector3d diff = to.subtract(from);
        double distanceXZ = Math.hypot(diff.x, diff.z);

        double yawRad = Math.atan2(diff.z, diff.x);
        float yaw = (float) Math.toDegrees(yawRad) - 90.0F;

        double pitchRad = Math.atan2(diff.y, distanceXZ);
        float pitch = (float) -Math.toDegrees(pitchRad);
        return new Vector2f(yaw, pitch);
    }

    public Vector2f getRotations(final Entity entity) {
        return this.getBoundingBox(entity);
    }

    public Vector2f randomRotate(final Vector2f currentRotation) {
        float shakeX = ThreadLocalRandom.current().nextFloat(-1, 1);
        float shakeY = ThreadLocalRandom.current().nextFloat(-1, 1);

        return new Vector2f(currentRotation.x + shakeX, currentRotation.y + shakeY);
    }

    public Vector2f correctRotation(Vector2f rotationVector) {
        float yaw = MathHelper.wrapDegrees(rotationVector.x);
        float pitch = Math.max(-90, Math.min(90, rotationVector.y));

        float gcd = (float) getGcd();
        yaw -= yaw % gcd;
        pitch -= pitch % gcd;

        return new Vector2f(yaw, pitch);
    }

    @Override
    public void update(LivingEntity target) {
        Vector2f rotations = getRotations(target);

        rotations = randomRotate(rotations);
        rotations = correctRotation(rotations);


        rotation = rotations;
    }
}
