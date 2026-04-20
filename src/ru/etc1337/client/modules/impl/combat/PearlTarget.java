package ru.etc1337.client.modules.impl.combat;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.game.Inventory;
import ru.etc1337.api.other.ScriptConstructor;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Pearl Target", description = "Бросает Эндер-Жемчуг за противником", category = ModuleCategory.COMBAT)
public class PearlTarget extends Module {

    private final BooleanSetting onlyTarget = new BooleanSetting("Только цель Aura", this);

    private final Timer timer = new Timer();
    private final ScriptConstructor script = new ScriptConstructor();

    private boolean cooldownCheck() {
        return !mc.player.getCooldownTracker().hasCooldown(Items.ENDER_PEARL);
    }


    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (cooldownCheck() && script.isFinished()) {
                aimAndThrowPearl();
            }
            script.update();
        }
    }


    public float[] calculateYawPitch(Vector3d targetPosition, double velocity) {
        Vector3d playerPosition = mc.player.getPositionVec();

        double deltaX = targetPosition.x - playerPosition.x;
        double deltaY = targetPosition.y - (playerPosition.y + mc.player.getEyeHeight());
        double deltaZ = targetPosition.z - playerPosition.z;

        float yaw = (float) (Math.atan2(deltaZ, deltaX) * (180 / Math.PI)) - 90;

        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float gravity = 0.03F;
        float pitch = (float) -Math.toDegrees(Math.atan((velocity * velocity - Math.sqrt(velocity * velocity * velocity * velocity - gravity * (gravity * horizontalDistance * horizontalDistance + 2 * deltaY * velocity * velocity))) / (gravity * horizontalDistance)));

        return new float[]{yaw, pitch};
    }

    public void aimAndThrowPearl() {
        Vector3d targetPearlLandingPosition = getTargetPearlLandingPosition();

        if (targetPearlLandingPosition != null && timer.finished(1000) && mc.player.getPositionVec().distanceTo(targetPearlLandingPosition) > 8) {
            float[] yawPitch = calculateYawPitch(targetPearlLandingPosition, 1.5F);
            int slot = Inventory.findItem(Items.ENDER_PEARL);
            boolean findPearl = slot != -1;
            Vector3d trajectoryPearl = checkTrajectory(yawPitch[0], yawPitch[1]);

            if (findPearl && trajectoryPearl != null && targetPearlLandingPosition.distanceTo(trajectoryPearl) > 8) {
                return;
            }


            Inventory.Use.useItem(slot, true, true, 7, yawPitch[0], yawPitch[1]);
            timer.reset();
        }
    }

    private Vector3d getTargetPearlLandingPosition() {
        for (Entity entity : mc.world.getAllEntities())
            if (entity instanceof EnderPearlEntity pearl)
                if (pearl.getShooter() != null && pearl.getShooter().equals(mc.player))
                    return null;

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof EnderPearlEntity pearl) {
                Entity shooter = pearl.getShooter();
                if (shooter != null && shooter != mc.player && (onlyTarget.isEnabled()  ? shooter == Client.getInstance().getModuleManager().get(KillAura.class).getTarget() : mc.player.getDistance(shooter) <= 6)) {
                    Vector3d pearlPosition = pearl.getPositionVec();
                    Vector3d pearlMotion = pearl.getMotion();
                    Vector3d lastPosition;

                    for (int i = 0; i <= 300; i++) {
                        lastPosition = pearlPosition;
                        pearlPosition = pearlPosition.add(pearlMotion);
                        pearlMotion = updatePearlMotion(pearl, pearlMotion, pearlPosition);

                        if (shouldEntityHit(pearlPosition, lastPosition) || pearlPosition.y <= 0) {
                            return lastPosition;
                        }
                    }
                }
            }
        }
        return null;
    }
    public boolean shouldEntityHit(Vector3d pearlPosition, Vector3d lastPosition) {
        final RayTraceContext rayTraceContext = new RayTraceContext(
                lastPosition,
                pearlPosition,
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                mc.player
        );
        final BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);

        return blockHitResult.getType() == RayTraceResult.Type.BLOCK;
    }
    public Vector3d updatePearlMotion(Entity entity, Vector3d originalPearlMotion, Vector3d pearlPosition) {
        Vector3d pearlMotion = originalPearlMotion;

        if ((entity.isInWater() || mc.world.getBlockState(new BlockPos(pearlPosition)).getBlock() == Blocks.WATER) && !(entity instanceof TridentEntity)) {
            float scale = entity instanceof EnderPearlEntity ? 0.8f : 0.6f;
            pearlMotion = pearlMotion.scale(scale);
        } else {
            pearlMotion = pearlMotion.scale(0.99f);
        }

        if (!entity.hasNoGravity())
            pearlMotion.y -= entity instanceof EnderPearlEntity ? 0.03 : 0.05;

        return pearlMotion;
    }
    private Vector3d checkTrajectory(float yaw, float pitch) {
        if (Float.isNaN(pitch))
            return null;
        float yawRad = yaw / 180.0f * 3.1415927f;
        float pitchRad = pitch / 180.0f * 3.1415927f;
        double x = mc.player.getPosX() - MathHelper.cos(yawRad) * 0.16f;
        double y = mc.player.getPosY() + mc.player.getEyeHeight(mc.player.getPose()) - 0.1;
        double z = mc.player.getPosZ() - MathHelper.sin(yawRad) * 0.16f;
        double motionX = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad) * 0.4f;
        double motionY = -MathHelper.sin(pitchRad) * 0.4f;
        double motionZ = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad) * 0.4f;
        final float distance = MathHelper.sqrt((float) (motionX * motionX + motionY * motionY + motionZ * motionZ));
        motionX /= distance;
        motionY /= distance;
        motionZ /= distance;
        motionX *= 1.5f;
        motionY *= 1.5f;
        motionZ *= 1.5f;
        if (!mc.player.isOnGround()) motionY += mc.player.getMotion().getY();
        return traceTrajectory(new Vector3d(x, y, z), new Vector3d(motionX, motionY, motionZ));
    }

    private Vector3d traceTrajectory(Vector3d pearlPos, Vector3d motion) {
        Vector3d lastPos;
        for (int i = 0; i <= 300; i++) {
            lastPos = pearlPos;
            pearlPos = pearlPos.add(motion);
            motion = updatePearlMotion(new EnderPearlEntity(mc.world, 0, 0, 0), motion, pearlPos);

            if (shouldEntityHit(pearlPos, lastPos) || pearlPos.y <= 0) {
                return pearlPos;
            }
        }
        return null;
    }

}