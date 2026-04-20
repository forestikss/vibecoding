package ru.etc1337.client.modules.impl.combat.killaura.rotation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.api.game.Maths;
import ru.etc1337.client.modules.impl.combat.killaura.rotation.api.Rotation;

public class SimpleRotation extends Rotation {

    @Override
    public void update(LivingEntity target) {
        if (Float.isNaN(this.rotation.x)) this.rotation.x = 0;
        if (Float.isNaN(this.rotation.y)) this.rotation.y = 0;

        Vector3d pos = target.getBoundingBox().getCenter().subtract(mc.player.getEyePosition(1.0F)).normalize();

        float shortestYawPath = (float) ((((((Math.toDegrees(Math.atan2(pos.z, pos.x)) - 90) - this.rotation.x) % 360) + 540) % 360) - 180);

        float findPitch = (float) Math.min(90, -Math.toDegrees(Math.atan2(pos.y, Math.hypot(pos.x, pos.z))));

        float targetYaw = this.rotation.x + shortestYawPath;
        float targetPitch = MathHelper.clamp(findPitch, -90, 90);

        targetPitch += Maths.random(-1, 1);
        targetYaw += Maths.random(-1, 1);


        Vector2f correctedRotation = correctRotation(
                targetYaw,
                targetPitch
        );
        if (!Float.isNaN(correctedRotation.x) && !Float.isNaN(correctedRotation.y)) {
            this.rotation = new Vector2f(correctedRotation.x, correctedRotation.y);
        }
        
    }
}
