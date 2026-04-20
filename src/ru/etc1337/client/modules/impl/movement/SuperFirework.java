package ru.etc1337.client.modules.impl.movement;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.animations.simple.Animation;
import ru.etc1337.api.animations.simple.ColorAnimation;
import ru.etc1337.api.animations.simple.Easing;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.draggable.Draggable;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.*;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.render.ui.dropui.AnimationMath;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.combat.ElytraTarget;
import ru.etc1337.client.modules.impl.combat.KillAura;
//import ru.kotopushka.compiler.sdk.annotations.Compile;
//import ru.kotopushka.compiler.sdk.annotations.VMProtect;
//import ru.kotopushka.compiler.sdk.enums.VMProtectType;

@ModuleInfo(name = "Super Firework", description = "Увеличивает силу фейерверка", category = ModuleCategory.MOVEMENT)
public class SuperFirework extends Module {

    private final SliderSetting fireworkSpeed = new SliderSetting("Firework Speed", this, 1.60F, 1.50F, 2.0F, 0.01F).setVisible((() -> !getSmartSpeed().isEnabled()));

    @Getter
    private final BooleanSetting smartSpeed = new BooleanSetting("Smart Speed", this);
    public final MultiModeSetting mode = new MultiModeSetting("Тип", this, "V1", "V2");
    private final BooleanSetting matrix = new BooleanSetting("Matrix", this).setVisible(() -> smartSpeed.isEnabled() && mode.get("V1").isEnabled());
    private final BooleanSetting untrusted = new BooleanSetting("Untrusted", this).setVisible(() -> smartSpeed.isEnabled() && mode.get("V1").isEnabled() && matrix.isEnabled());


    private final SliderSetting fireworkSpeedMinGlobal = new SliderSetting("Min Speed Global", this, 1.65F, 1.50F, 5.0F, 0.01F).setVisible(() -> smartSpeed.isEnabled() && mode.get("V2").isEnabled());
    private final SliderSetting fireworkSpeedMaxGlobal = new SliderSetting("Max Speed Global", this, 2.1F, 1.50F, 5.0F, 0.01F).setVisible(() -> smartSpeed.isEnabled() && mode.get("V2").isEnabled());
    private final SliderSetting fireworkSpeedMaxYaw = new SliderSetting("Max Speed Yaw", this, 1.95F, 1.50F, 5.0F, 0.01F).setVisible(() -> smartSpeed.isEnabled() && mode.get("V2").isEnabled());
    private final SliderSetting fireworkSpeedMaxPitch = new SliderSetting("Max Speed Pitch", this, 2.5F, 1.50F, 5.0F, 0.01F).setVisible(() -> smartSpeed.isEnabled() && mode.get("V2").isEnabled());


    @Override
    public void onEvent(Event event) {
        if (Client.getInstance().getModuleManager().get(AntiTarget.class).isEnabled()) return;

        if (!(event instanceof EventFireworkMotion fireworkMotion)) return;
        ElytraTarget elytraTarget = Client.getInstance().getModuleManager().get(ElytraTarget.class);
        KillAura killAura = Client.getInstance().getModuleManager().get(KillAura.class);

        boolean boosterWorking = true;
        if (elytraTarget.shouldTarget(killAura.getTarget())/* && elytraTarget.getReallyWorld().isEnabled()*/) {
            Vector3d targetVec = killAura.getElytraRotation().getTargetVec();
            if (targetVec == null) return;

            final double currentDistance = mc.player.getPositionVec().distanceTo(targetVec);
            boolean canAttack = currentDistance <= 1.0F;
            if (canAttack) {
                boosterWorking = false;
            }
        }

        double speedXZ = fireworkSpeed.getValue();
        double speedY = fireworkSpeed.getValue();

        if (smartSpeed.isEnabled()) {
            if (mode.get("V1").isEnabled() && mode.get("V2").isEnabled()) {
                speedXZ = Math.max(getBoostV1(), getBoostV2());
            } else if (mode.get("V1").isEnabled())  {
                speedXZ = getBoostV1();
            } else if (mode.get("V2").isEnabled()) {
                speedXZ = getBoostV2();
            }

            speedY = 1.6F;
        }

        if (mode.get("V2").isEnabled()) {
            speedXZ = Math.max(fireworkSpeedMinGlobal.getValue(), speedXZ);
            speedXZ = Math.min(fireworkSpeedMaxGlobal.getValue(), speedXZ);
        }

        fireworkMotion.setVector3d(new Vector3d(speedXZ, speedY, speedXZ));
        fireworkMotion.setCancelled(boosterWorking);
    }

    private static final int[] YAW_VECTORS = {-45, 45, 135, -135};
    private static final int[] PITCH_VECTORS = {-45, 45};

    public double getBoostV2() {
        KillAura killAura = Client.getInstance().getModuleManager().get(KillAura.class);
        LivingEntity target = killAura.getTarget();
        float lastYaw = target != null ? killAura.getRotationVector().x : mc.player.rotationYaw;
        float lastPitch = target != null ? killAura.getRotationVector().y : mc.player.rotationPitch;

        if (Math.abs(lastPitch) > 55) {
            return 1.55D;
        }

        double yawRad = Math.toRadians(lastYaw);
        double pitchRad = Math.toRadians(lastPitch);

        double sinYaw = Math.sin(yawRad);
        double cosYaw = Math.cos(yawRad);
        double cosPitch = Math.cos(pitchRad);

        if (cosPitch < 1e-6) {
            return 1.55D;
        }

        double m = Math.max(Math.abs(sinYaw), Math.abs(cosYaw));

        double pitch_contrib = (1.0 / cosPitch) - 1.0;
        double yaw_contrib = (1.0 / m) - 1.0;

        /*if (matrix.isEnabled())*/ {
            double a = 0.15D;
            double b = 1.45D;
            double desired_yaw_max_boost = fireworkSpeedMaxYaw.getValue();
            double desired_pitch_max_boost = fireworkSpeedMaxPitch.getValue();

            double yaw_max_contrib = (desired_yaw_max_boost - a) / b - 1.0D;
            double pitch_max_contrib = (desired_pitch_max_boost - a) / b - 1.0D;

            pitch_contrib = Math.min(pitch_contrib, pitch_max_contrib);
            yaw_contrib = Math.min(yaw_contrib, yaw_max_contrib);
        }

        double inv = 1.0 + pitch_contrib + yaw_contrib;

        double a = 0.15;
        double b = 1.45;
        double boost = a + b * inv;

        //boost = Math.max(1.6D, boost); // min - 1.6D закоментим, т.к в дальнейшем мы установим лимит
        return boost;
    }

    public double getBoostV1() {
        double boost = 0;

        KillAura killAura = Client.getInstance().getModuleManager().get(KillAura.class);
        LivingEntity target = killAura.getTarget();
        float lastYaw = target != null ? killAura.getRotationVector().x : mc.player.rotationYaw;
        float lastPitch = target != null ? killAura.getRotationVector().y : mc.player.rotationPitch;

        if (Math.abs(lastPitch) > 55) {
            return 1.55D;
        }

        float boostYaw = adjustBoostForYaw(lastYaw);
        double boostPitch = adjustBoostForPitch(lastYaw, lastPitch);

        boost = boostYaw + (boostPitch - 1.6f);
        boost = Math.max(1.6D, boost);
        return /*Math.min(boost, 2.2D)*/matrix.isEnabled() ? Math.min(boost, 2.1D) : boost;
    }

    private float adjustBoostForYaw(float lastYaw) {
        int closestYawIndex = findClosestVector(lastYaw, YAW_VECTORS);
        if (closestYawIndex == -1) {
            return 1.6F;
        }

        float yawDistance = Math.abs(MathHelper.wrapDegrees(lastYaw) - YAW_VECTORS[closestYawIndex]);
        float maxBoost = 2.2f;
        float minBoostValue = 1.6f;
        float maxDistance = 12f;
        float variablespeedSmart = 0;
        if (yawDistance <= maxDistance) {
            float ratio = yawDistance / maxDistance;
            variablespeedSmart = maxBoost - (maxBoost - minBoostValue) * ratio;
        }


        float variableSpeed = getVariableSpeed(yawDistance);
        float finalSpeed = Math.max(variablespeedSmart, variableSpeed);

        float max = untrusted.isEnabled() ? 1.95F : 1.8F; // в матриксе лимит 2.0 но где то 1.8 - 2.0, например bravohvh/rw
        return matrix.isEnabled() ? Math.min(finalSpeed, max) : finalSpeed;
    }

    private static float getVariableSpeed(float yawDistance) {
        float[] thresholds = {4, 8, 11, 15, 21, 28};
        float[] speeds = {2.2f, 2.1f, 2.0f, 1.9f, 1.8f, 1.7f, 1.6f};
        int level = 0;
        while (level < thresholds.length && yawDistance >= thresholds[level]) {
            level++;
        }
        return speeds[level];
    }

    private double adjustBoostForPitch(float lastYaw, float lastPitch) {

        int closestYawIndex = findClosestVector(lastPitch, PITCH_VECTORS);
        if (closestYawIndex == -1) {
            return 1.6F;
        }

        int closestYawIndex1 = findClosestVector(lastYaw, YAW_VECTORS);
        float yawDistance1 = Math.abs(MathHelper.wrapDegrees(lastYaw) - YAW_VECTORS[closestYawIndex1]);


        float yawDistance = Math.abs(MathHelper.wrapDegrees(lastPitch) - PITCH_VECTORS[closestYawIndex]);
        float maxBoost = /*yawDistance1 < 4 ? 1.9f : getVariableSpeed(yawDistance)*/getVariableSpeed(yawDistance);
        float minBoostValue = 1.6f;
        float maxDistance = 45;
        float variablespeedSmart = 0;
        if (yawDistance <= maxDistance) {
            float ratio = yawDistance / maxDistance;
            variablespeedSmart = maxBoost - (maxBoost - minBoostValue) * ratio;
        }
        ///Chat.send(String.valueOf(yawDistance1));

        return variablespeedSmart;
    }

    private static int findClosestVector(float angle, int[] vectors) {
        int minDistIndex = -1;
        float minDist = Float.MAX_VALUE;

        for (int i = 0; i < vectors.length; i++) {
            float dist = Math.abs(MathHelper.wrapDegrees(angle) - vectors[i]);
            if (dist < minDist) {
                minDist = dist;
                minDistIndex = i;
            }
        }

        return minDistIndex;
    }
}

