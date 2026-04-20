package ru.etc1337.api.game;

import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.api.game.maths.CustRandom;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.mods.fastrandom.FastRandom;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class Maths implements QuickImports {
    public static FastRandom fastRandomize = new FastRandom();

    public double roundHalfUp(double num, double increment) {
        double v = (double) java.lang.Math.round(num / increment) * increment;
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public float interpolate(double oldValue, double newValue, double interpolationValue){
        return (float) (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public int getPercentage(int percentage, int totalValue) {
        return (int) (totalValue * (percentage / 100.0F));
    }
    public float randomNew(double min, double max) {
        if (min > max) return (float) (fastRandomize.nextFloat() * (min - max) + max);
        return (float) (fastRandomize.nextFloat() * (max - min) + min);
    }

    public float[] getRotVec(Vector3d pos, Vector3d vec) {
        if (vec == null)
            return new float[] { mc.player.rotationYaw, mc.player.rotationPitch };
        double posX = vec.getX() - pos.x;
        double posY = vec.getY() - (pos.y + (double) mc.player.getEyeHeight());
        double posZ = vec.getZ() - pos.z;
        double sqrt = MathHelper.sqrt(posX * posX + posZ * posZ);
        float yaw = (float) (Math.atan2(posZ, posX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(posY, sqrt) * 180.0 / Math.PI));
        float sens = (float) (mc.getGameSettings().mouseSensitivity * 0.6f + 0.2f);
        float pow = sens * sens * sens * 1.2F;
        yaw -= yaw % pow;
        pitch -= pitch % (pow * sens);
        return new float[] { yaw, pitch };
    }

    public int lerp(int input, int target, double step) {
        return (int) (input + step * (target - input));
    }

    public boolean canSeen(Vector3d vec)
    {
        Vector3d vector3d = mc.player.getPositionVec().add(0,mc.player.getEyeHeight(),0);
        return mc.world.rayTraceBlocks(new RayTraceContext(vector3d, vec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, mc.player)).getType() == RayTraceResult.Type.MISS;
    }
    public double round(double target, int decimal) {
        return Math.round(target * Math.pow(10, decimal)) / Math.pow(10, decimal);
    }
    public float random(float min, float max) {
        return (new CustRandom().randomNumber(0,1,false) * (max - min) + min);
    }
    public double randomSimple(double min, double max) {
        return (Math.random() * (max - min)) + min;
    }
    public int randomInt(int min, int max) {
        return (int) (min + (max - min) * Math.random());
    }



    public <T extends Number> T lerp(T input, T target, double step) {
        double start = input.doubleValue();
        double end = target.doubleValue();
        double result = start + step * (end - start);

        if (input instanceof Integer) {
            return (T) Integer.valueOf((int) Math.round(result));
        } else if (input instanceof Double) {
            return (T) Double.valueOf(result);
        } else if (input instanceof Float) {
            return (T) Float.valueOf((float) result);
        } else if (input instanceof Long) {
            return (T) Long.valueOf(Math.round(result));
        } else if (input instanceof Short) {
            return (T) Short.valueOf((short) Math.round(result));
        } else if (input instanceof Byte) {
            return (T) Byte.valueOf((byte) Math.round(result));
        } else {
            throw new IllegalArgumentException("Unsupported type: " + input.getClass().getSimpleName());
        }
    }
}
