package ru.etc1337.client.modules.impl.combat.killaura.rotation.api;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.client.modules.impl.combat.ElytraTarget;

@Getter @Setter
public abstract class Rotation implements QuickImports {
    public Vector2f rotation = Vector2f.ZERO;

    public abstract void update(LivingEntity target);

    public static double getGcd() {
        double sensitivity = mc.gameSettings.mouseSensitivity;
        double value = sensitivity * 0.6 + 0.2;
        double result = Math.pow(value, 1.5F) * 0.8;
        return result * 0.15D;
    }

    public static Vector2f correctRotation(float yaw, float pitch) {
        if ((yaw == -90 && pitch == 90) || yaw == -180) return new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);

        float gcd = (float) getGcd();
        yaw -= yaw % gcd;
        pitch -= pitch % gcd;

        return new Vector2f(yaw, pitch);
    }

    public void onEvent(Event event) { }
    public void attacked() { }
}
