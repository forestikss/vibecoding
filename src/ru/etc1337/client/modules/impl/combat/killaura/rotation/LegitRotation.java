package ru.etc1337.client.modules.impl.combat.killaura.rotation;

import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.Client;
import ru.etc1337.api.game.RayTrace;
import ru.etc1337.client.modules.impl.combat.KillAura;
import ru.etc1337.client.modules.impl.combat.killaura.rotation.api.Rotation;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

public class LegitRotation extends Rotation {
    @Getter
    private SecureRandom secureRandom = new SecureRandom();
    private float lastYawDelta = 0.0f;
    private float lastPitchDelta = 0.0f;
    private int rotationCount = 0;
    private float pitchNoiseAccumulator = 0.0f;
    private float yawNoiseAccumulator = 0.0f;
    private float lastSpeedFactor = 1.0f;
    private float dynamicSmoothing = 0.35f;

    // Генерация псевдослучайных значений с нормальным распределением
    private float randomGaussian(float min, float max) {
        float value = (float) secureRandom.nextGaussian();
        value = MathHelper.clamp(value, -2.0f, 2.0f) / 4.0f + 0.5f;
        return min + (max - min) * value;
    }

    // Быстрый рандом для менее важных значений
    private float fastRandom(float min, float max) {
        return min + (max - min) * ThreadLocalRandom.current().nextFloat();
    }

    public void randomizeSeed() {
        this.secureRandom = new SecureRandom();
        this.dynamicSmoothing = randomGaussian(0.3f, 0.5f);
    }

    @Override
    public void update(LivingEntity target) {
        KillAura killAura = Client.getInstance().getModuleManager().get(KillAura.class);

        // Инициализация начальных значений
        if (Float.isNaN(this.rotation.x)) this.rotation.x = mc.player.rotationYaw;
        if (Float.isNaN(this.rotation.y)) this.rotation.y = mc.player.rotationPitch;

        // Вычисление вектора атаки с небольшим рандомным смещением
        Vector3d vec = getBestVector(target, randomGaussian(0.12f, 0.22f));

        // Вычисление углов поворота
        float shortestYawPath = wrapDegrees((float) Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90 - this.rotation.x);
        float yawToTarget = rotation.x + shortestYawPath;
        float pitchToTarget = (float) -Math.toDegrees(Math.atan2(vec.y, Math.hypot(vec.z, vec.x)));

        // Разница между текущим и целевым углом
        float yawDelta = wrapDegrees(yawToTarget - rotation.x);
        float pitchDelta = wrapDegrees(pitchToTarget - rotation.y);

        // Динамическая чувствительность
        float sensitivity = randomGaussian(0.7f, 0.9f);
        float speed = getSens(sensitivity);

        // Учет препятствий
        float rayCaster = RayTrace.rayTraceWithBlock(killAura.getMaxDistance(),
                rotation.x, rotation.y, mc.player, target) ? 0.75f : 1.0f;

        // Максимальная скорость поворота с динамическими ограничениями
        float maxYawSpeed = randomGaussian(45.0f, 65.0f);
        float maxPitchSpeed = randomGaussian(8.0f, 12.0f);

        // Ограничение скорости поворота
        float clampYaw = Math.min(Math.abs(yawDelta), maxYawSpeed) * speed * rayCaster;
        float clampPitch = Math.min(Math.abs(pitchDelta), maxPitchSpeed) * speed * rayCaster;

        yawDelta = yawDelta > 0 ? clampYaw : -clampYaw;
        pitchDelta = pitchDelta > 0 ? clampPitch : -clampPitch;

        // Динамический фактор скорости
        float speedFactor = 1.0f;
        if (rotationCount % 4 == 0) {
            speedFactor = randomGaussian(0.9f, 1.1f);
            lastSpeedFactor = speedFactor;
        } else {
            speedFactor = lastSpeedFactor;
        }

        // Применение настроек скорости из KillAura
        yawDelta /= (killAura.universalSpeedX.getValue() * speedFactor);
        pitchDelta /= (killAura.universalSpeedY.getValue() * speedFactor);

        // Добавление небольшого рандомного шума
        yawDelta += randomGaussian(-0.12f, 0.12f);
        pitchDelta += randomGaussian(-0.15f, 0.15f);

        // Динамическое сглаживание
        yawDelta = smoothDelta(yawDelta, lastYawDelta, dynamicSmoothing);
        pitchDelta = smoothDelta(pitchDelta, lastPitchDelta, dynamicSmoothing);

        // Гарантия минимального движения
        yawDelta = ensureNonZero(yawDelta, 0.12f);
        pitchDelta = ensureNonZero(pitchDelta, 0.12f);

        // Вычисление новых углов
        float yaw = rotation.x + yawDelta;
        float pitch = MathHelper.clamp(rotation.y + pitchDelta, -89.5f, 89.5f);

        // Накопленный шум для более естественного движения
        yawNoiseAccumulator += randomGaussian(-0.03f, 0.03f);
        yawNoiseAccumulator = MathHelper.clamp(yawNoiseAccumulator, -0.08f, 0.08f);
        pitchNoiseAccumulator += randomGaussian(-0.03f, 0.03f);
        pitchNoiseAccumulator = MathHelper.clamp(pitchNoiseAccumulator, -0.08f, 0.08f);

        yaw += yawNoiseAccumulator;
        pitch += pitchNoiseAccumulator;

        // Избегание "красивых" чисел (0.0, 0.5 и т.д.)
        if (Math.abs(pitch) < 0.18f || Math.abs(pitch % 0.01f) < 0.005f) {
            pitch += randomGaussian(0.06f, 0.1f);
        }
        if (Math.abs(yaw % 0.01f) < 0.005f) {
            yaw += randomGaussian(0.06f, 0.1f);
        }

        // Коррекция углов
        yaw = correctRotation(yaw);
        pitch = correctRotation(pitch);

        // Обновление текущих углов
        rotation.x = yaw;
        rotation.y = pitch;
        rotation = correctRotation(rotation.x, rotation.y);

        // Сохранение дельты для следующего кадра
        lastYawDelta = yawDelta;
        lastPitchDelta = pitchDelta;
        rotationCount++;

        
    }

    @Override
    public void attacked() {
        randomizeSeed();
        super.attacked();
    }

    public static Vector3d getBestVector(LivingEntity target, float jitterOnBoxValue) {
        double yExpand = clamp(mc.player.getPosYEye() - target.getPosYEye(),
                target.getHeight() / 2, target.getHeight()) /
                (mc.player.isElytraFlying() ? 10 :
                        !mc.gameSettings.keyBindJump.isKeyDown() && mc.player.isOnGround() ?
                                target.isSneaking() ? 0.8f : 0.6f : 1f);

        Vector3d finalVector = target.getPositionVec().add(0, yExpand, 0);
        return finalVector.add(jitterOnBoxValue, jitterOnBoxValue / 2, jitterOnBoxValue)
                .subtract(mc.player.getEyePosition(1)).normalize();
    }

    public float correctRotation(float rot) {
        rot = getSens(rot);
        rot += randomGaussian(-0.05f, 0.05f);
        return rot;
    }

    public float getSens(float rotation) {
        float gcd = getGc2d();
        float rounded = (float) (Math.round(rotation / gcd) * gcd);
        // Добавляем небольшую вариацию к округленному значению
        return rounded + randomGaussian(-0.01f, 0.01f);
    }

    private float getGc2d() {
        return fastRandom(0.12f, 0.18f); // Больший разброс GCD
    }

    private float smoothDelta(float current, float last, float smoothFactor) {
        return last + (current - last) * smoothFactor;
    }

    private float ensureNonZero(float delta, float min) {
        if (Math.abs(delta) < min) {
            return delta >= 0 ? min : -min;
        }
        return delta;
    }
}