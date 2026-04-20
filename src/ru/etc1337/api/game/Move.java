package ru.etc1337.api.game;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.MathHelper;
import ru.etc1337.api.interfaces.QuickImports;

@UtilityClass
public class Move implements QuickImports {
    public boolean isMoving() {
        return mc.player.movementInput.moveStrafe != 0.0 || mc.player.movementInput.moveForward != 0.0;
    }
    public static void setMotion(double motion) {
        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            mc.player.setMotion(0.0, mc.player.getMotion().y, 0.0);
            return;
        }
        if (forward != 0.0) {
            if (strafe > 0.0) {
                yaw += forward > 0.0 ? -45.0f : 45.0f;
            } else if (strafe < 0.0) {
                yaw += forward > 0.0 ? 45.0f : -45.0f;
            }
            strafe = 0.0;
            forward = Math.signum(forward);
        }
        double yawRad = Math.toRadians(yaw + 90.0f);
        double cosYaw = MathHelper.cos((float) yawRad);
        double sinYaw = MathHelper.sin((float) yawRad);
        mc.player.setMotion(forward * motion * cosYaw + strafe * motion * sinYaw, mc.player.getMotion().y, forward * motion * sinYaw - strafe * motion * cosYaw);
    }

    public static double[] forward(final double d, float f3) {
        double f = mc.player.movementInput.moveForward;
        double f2 = mc.player.movementInput.moveStrafe;
        // f3 - rotationYaw;

        if (f != 0.0f) {
            if (f2 > 0.0f) {
                f3 += ((f > 0.0f) ? -45 : 45);
            } else if (f2 < 0.0f) {
                f3 += ((f > 0.0f) ? 45 : -45);
            }
            f2 = 0.0f;
            if (f > 0.0f) {
                f = 1.0f;
            } else if (f < 0.0f) {
                f = -1.0f;
            }
        }
        final double d2 = Math.sin(Math.toRadians(f3 + 90.0f));
        final double d3 = Math.cos(Math.toRadians(f3 + 90.0f));
        final double d4 = f * d * d3 + f2 * d * d2;
        final double d5 = f * d * d2 - f2 * d * d3;
        return new double[]{d4, d5};
    }
    public double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }
    public void setSpeed(double moveSpeed) {
        setSpeed(moveSpeed, mc.player.rotationYaw, mc.player.movementInput.moveStrafe, mc.player.movementInput.moveForward);
    }
    public void setSpeed(double moveSpeed, float yaw, double strafe, double forward) {
        if (forward != 0.0D) {
            yaw += strafe > 0.0D ? (forward > 0.0D ? -45 : 45) : (strafe < 0.0D ? (forward > 0.0D ? 45 : -45) : 0);
            strafe = 0.0D;
            forward = forward > 0.0D ? 1.01D : (forward < 0.0D ? -1.01D : 0.0D);
        }
        strafe = strafe > 0.0D ? 1.0D : (strafe < 0.0D ? -1.0D : 0.0D);
        double mx = Math.cos(Math.toRadians(yaw + 90.0F)), mz = Math.sin(Math.toRadians(yaw + 90.0F));
        mc.player.getMotion().x = forward * moveSpeed * mx + strafe * moveSpeed * mz;
        mc.player.getMotion().z = forward * moveSpeed * mz - strafe * moveSpeed * mx;
    }
}
