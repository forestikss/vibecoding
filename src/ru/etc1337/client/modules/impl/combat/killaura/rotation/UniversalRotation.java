package ru.etc1337.client.modules.impl.combat.killaura.rotation;

import lombok.Getter;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.Client;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.game.Maths;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.game.RayTrace;
import ru.etc1337.client.modules.impl.combat.KillAura;
import ru.etc1337.client.modules.impl.combat.killaura.rotation.api.Rotation;

import java.security.SecureRandom;

import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

public class UniversalRotation extends Rotation {
    @Getter
    private SecureRandom secureRandom = new SecureRandom();

    private float randomizeValue(float min, float max) {
        return min + (max - min) * (float) Math.pow(secureRandom.nextFloat(), 1.2 + secureRandom.nextFloat() * 0.3);
    }

    public void randomizeSeed() {
        this.secureRandom = new SecureRandom();
    }

    @Override
    public void update(LivingEntity target) {
        KillAura killAura = Client.getInstance().getModuleManager().get(KillAura.class);
        if (Float.isNaN(this.rotation.x)) this.rotation.x = 0;
        if (Float.isNaN(this.rotation.y)) this.rotation.y = 0;

        Vector3d vec = getBestVector(target, 0);
     //   rotation.x = mc.player.headYaw; rotation.y = mc.player.pich;

        float shortestYawPath = (float) ((((((Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90) - this.rotation.x) % 360) + 540) % 360) - 180);
        float yawToTarget = rotation.x + shortestYawPath;
        float pitchToTarget = (float) -Math.toDegrees(Math.atan2(vec.y, Math.hypot(vec.z, vec.x)));

        float yawDelta = (wrapDegrees(yawToTarget - rotation.x));
        float pitchDelta = (wrapDegrees(pitchToTarget - rotation.y));

        float yaw, pitch, speed = getSens(randomizeValue(0.6F, 0.8F)), clampYaw, clampPitch,
                testCountSpeed = getSens(randomizeValue(130, 155));
        float rayCaster = RayTrace.rayTraceWithBlock(killAura.getMaxDistance(), rotation.x, rotation.y, mc.player, target) ? 0.4f : 1;

        //clampYaw = Math.min(Math.abs(yawDelta), testCountSpeed) * speed;
        clampYaw = Math.min(Math.abs(yawDelta), randomizeValue(123.133F, 126.477F)) * speed / testCountSpeed * 180 * rayCaster;

        yawDelta = yawDelta > 0 ? clampYaw : -clampYaw;

        yawDelta /= killAura.universalSpeedX.getValue(); // speed yaw

        clampPitch = Math.min(Math.abs(pitchDelta), randomizeValue(23.133F, 26.477F)) * speed / testCountSpeed * 90 * rayCaster;
        pitchDelta = pitchDelta > 0 ? clampPitch : -clampPitch;

        pitchDelta /= killAura.universalSpeedY.getValue(); // speed pitch

        if (pitchDelta > 0) pitchDelta += randomizeValue(-0.826F, 0.459F);

        yawDelta = fixDeltaNonVanillaMouse(yawDelta, pitchDelta).x;
        pitchDelta = fixDeltaNonVanillaMouse(yawDelta, pitchDelta).y;

        yaw = rotation.x + yawDelta;
        pitch = MathHelper.clamp(rotation.y + pitchDelta, -90, 90);

        yaw = correctRotation(yaw); pitch = correctRotation(pitch);

        rotation.x = yaw; rotation.y = pitch;
        rotation = correctRotation(rotation.x, rotation.y);

        
    }

    @Override
    public void attacked() {
        randomizeSeed();
        super.attacked();
    }

    public static Vector3d getBestVector(LivingEntity target, float jitterOnBoxValue) {
        double yExpand = clamp(mc.player.getPosYEye() - target.getPosYEye(), target.getHeight() / 2, target.getHeight())
                / (mc.player.isElytraFlying() ? 10 : !mc.gameSettings.keyBindJump.isKeyDown() && mc.player.isOnGround() ?
                target.isSneaking() ? 0.8F : 0.6f : 1F);

        Vector3d finalVector = target.getPositionVec().add(0, yExpand, 0);
        return finalVector.add(jitterOnBoxValue, jitterOnBoxValue / 2, jitterOnBoxValue).subtract(mc.player.getEyePosition(1)).normalize();
    }
    public Vector2f fixDeltaNonVanillaMouse(float delta, float secondDelta) {
        float value = (float) (randomizeValue(0.1F, 0.8F) + Math.pow(randomizeValue(-0.3F, 0.3F), 3));
        if (Math.abs(delta) > 0 && Math.abs(secondDelta) == 0) secondDelta += value;
        if (Math.abs(secondDelta) > 0 && Math.abs(delta) == 0) delta += value;

        return new Vector2f(delta, secondDelta);
    }
    public float correctRotation(float rot) {
        rot = getSens(rot);
        rot -= (float) (rot % getGcd());

        return rot;
    }

    public float getSens(float rotation) {
        return (float) (getDeltaMouse(rotation) * getGcd());
    }

    public float getDeltaMouse(float delta) {
        return Math.round(delta / getGcd());
    }

}
