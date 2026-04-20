package ru.etc1337.client.modules.impl.movement;

import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventMotion;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

import java.util.concurrent.ThreadLocalRandom;

@ModuleInfo(name = "Grim Glide", description = ":/", category = ModuleCategory.MOVEMENT)
public class GrimGlide extends Module {
    private final Timer ticks = new Timer();
    private int ticksTwo = 0;

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventMotion)) return;
        if (mc.player == null || mc.world == null || !mc.player.isElytraFlying()) return;

        ticksTwo++;
        Vector3d pos = mc.player.getPositionVec();
        float yaw = mc.player.rotationYaw;
        double forward = 0.087;
        double motion = Player.getBps(mc.player);

        // isReallyWorld: если есть сервер (не singleplayer) — порог 48, иначе 52
        float maxBps = mc.isSingleplayer() ? 52f : 48f;

        if (motion >= maxBps) {
            forward = 0f;
            motion = 0;
        }

        double dx = -Math.sin(Math.toRadians(yaw)) * forward;
        double dz =  Math.cos(Math.toRadians(yaw)) * forward;

        mc.player.setVelocity(
                dx * rnd(1.1f, 1.21f),
                mc.player.getMotion().y - 0.02f,
                dz * rnd(1.1f, 1.21f)
        );

        if (ticks.finished(50)) {
            mc.player.setPosition(
                    pos.getX() + dx,
                    pos.getY(),
                    pos.getZ() + dz
            );
            ticks.reset();
        }

        mc.player.setVelocity(
                dx * rnd(1.1f, 1.21f),
                mc.player.getMotion().y + 0.016f,
                dz * rnd(1.1f, 1.21f)
        );
    }

    private double rnd(float min, float max) {
        return min + ThreadLocalRandom.current().nextFloat() * (max - min);
    }
}
