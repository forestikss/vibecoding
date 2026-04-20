package ru.etc1337.client.modules.impl.movement;

import lombok.Getter;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.api.animations.simple.Animation;
import ru.etc1337.api.animations.simple.Easing;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventFireworkMotion;
import ru.etc1337.api.events.impl.game.EventMotion;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;

import static ru.etc1337.api.events.impl.game.EventMotion.lastPitch;

@ModuleInfo(name = "Anti Target", description = "Улетает на гаити", category = ModuleCategory.MOVEMENT)
public class AntiTarget extends Module {
    public SliderSetting angle = new SliderSetting("Угол", this, 45, -45, 45, 1);
    public SliderSetting speed = new SliderSetting("Скорость", this, 0.5f, 0, 1, 0.1f);

    @Override
    public void onEvent(Event event) {
        if (mc.player != null && mc.world != null && mc.player.isElytraFlying()) {
            Player.look(event, new Vector2f(mc.player.rotationYaw, -angle.getValue()), Player.Correction.FULL, null);
        }
        if (event instanceof EventFireworkMotion fireworkMotion) {
            if (lastPitch < -45 || lastPitch > 45) return;
            final LivingEntity entity = fireworkMotion.getEntity();
            if (!(entity instanceof ClientPlayerEntity player)) return;

            double speed = getBoost();
            fireworkMotion.setVector3d(new Vector3d(speed, speed, speed));
            fireworkMotion.setCancelled(true);
        }
    }


    private static final int[] PITCH_VECTORS = {-45, 45};


    @Compile
    public double getBoost() {
        float boost = 1.5f;

        float lastPitch = EventMotion.lastPitch;
        boost = adjustBoostForPitch(lastPitch, boost);

        return boost;
    }

    @Compile
    private float adjustBoostForPitch(float lastPitch, float boost) {

        int closestPitchIndex = findClosestVector(lastPitch, PITCH_VECTORS);
        float pitchDistance = Math.abs(Math.abs(lastPitch) - Math.abs(PITCH_VECTORS[closestPitchIndex]));
        //+- OK
        float min = 40; // default: 30
        float inc = speed.getValue(); // 1F = 52 MAX, default: 0.525F
        if (pitchDistance < min) {
            boost += inc * (1 - pitchDistance / min);
        }


        return boost;
    }

    @Compile
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
