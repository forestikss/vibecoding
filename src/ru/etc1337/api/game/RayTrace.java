package ru.etc1337.api.game;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import ru.etc1337.api.interfaces.QuickImports;

import java.util.Optional;
import java.util.function.Predicate;

@UtilityClass
public class RayTrace implements QuickImports {
    public RayTraceResult rayTrace(double rayTraceDistance,
                                   float yaw,
                                   float pitch,
                                   Entity entity) {
        Vector3d startVec = mc.player.getEyePosition(1.0F);
        Vector3d directionVec = getVectorForRotation(pitch, yaw);
        Vector3d endVec = startVec.add(
                directionVec.x * rayTraceDistance,
                directionVec.y * rayTraceDistance,
                directionVec.z * rayTraceDistance
        );

        return mc.world.rayTraceBlocks(new RayTraceContext(
                startVec,
                endVec,
                RayTraceContext.BlockMode.OUTLINE,
                RayTraceContext.FluidMode.NONE,
                entity)
        );
    }


    public static boolean tracedTo(Entity shooter, Vector3d startVec, Vector3d endVec, AxisAlignedBB boundingBox, Predicate<Entity> filter, double distance, Entity target)
    {
        World world = shooter.world;
        double d0 = distance;

        for (Entity entity1 : world.getEntitiesInAABBexcluding(shooter, boundingBox, filter))
        {


            AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow((double)entity1.getCollisionBorderSize());
            Optional<Vector3d> optional = axisalignedbb.rayTrace(startVec, endVec);

            if (axisalignedbb.contains(startVec))
            {

                if (d0 >= 0.0D)
                {
                    if (entity1 == target) return true;
                    d0 = 0.0D;
                }
            }
            else if (optional.isPresent())
            {

                Vector3d vector3d1 = optional.get();
                double d1 = startVec.squareDistanceTo(vector3d1);

                //if (d1 < d0 || d0 == 0.0D)
                {

                    if (entity1.getLowestRidingEntity() == shooter.getLowestRidingEntity())
                    {
                        if (d0 == 0.0D)
                        {
                            if (entity1 == target) return true;
                        }
                    }
                    else
                    {
                        if (entity1 == target) return true;
                        d0 = d1;
                    }
                }
            }
        }

        return false;
    }

    public boolean rayTraceWithBlock(double rayTraceDistance, float yaw, float pitch, Entity entity, Entity target) {

        RayTraceResult object = null;
        if (target == null) return false;
        if (entity != null && mc.world != null) {
            float partialTicks = mc.getRenderPartialTicks();
            double distance = rayTraceDistance;
            object = rayTrace(rayTraceDistance, yaw, pitch, entity);
            Vector3d vector3d = entity.getEyePosition(partialTicks);
            boolean flag = false;
            double d1 = distance;

            /*
            if (mc.playerController.extendedReach()) {
                d1 = 6.0D;
                distance = d1;
            }
            */

            d1 = d1 * d1;

            if (object != null) {
                d1 = object.getHitVec().squareDistanceTo(vector3d);
            }

            Vector3d vector3d1 = getVectorForRotation(pitch, yaw);
            Vector3d vector3d2 = vector3d.add(vector3d1.x * distance, vector3d1.y * distance, vector3d1.z * distance);
            float f = 1.0F;
            AxisAlignedBB axisalignedbb = entity.getBoundingBox().expand(vector3d1.scale(distance)).grow(1.0D, 1.0D, 1.0D);
            boolean traced = tracedTo(entity, vector3d, vector3d2, axisalignedbb, (p_lambda$getMouseOver$0_0_) ->
            {
                return !p_lambda$getMouseOver$0_0_.isSpectator() && p_lambda$getMouseOver$0_0_.canBeCollidedWith();
            }, d1, target);

            return traced;
        }

        return false;
    }

    public Vector3d getVectorForRotation(float pitch, float yaw) {
        float yawRadians = -yaw * ((float) Math.PI / 180) - (float) Math.PI;
        float pitchRadians = -pitch * ((float) Math.PI / 180);

        float cosYaw = MathHelper.cos(yawRadians);
        float sinYaw = MathHelper.sin(yawRadians);
        float cosPitch = -MathHelper.cos(pitchRadians);
        float sinPitch = MathHelper.sin(pitchRadians);

        return new Vector3d(sinYaw * cosPitch, sinPitch, cosYaw * cosPitch);
    }
}
