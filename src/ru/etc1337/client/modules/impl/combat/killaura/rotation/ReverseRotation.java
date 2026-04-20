package ru.etc1337.client.modules.impl.combat.killaura.rotation;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import ru.etc1337.Client;
import ru.etc1337.api.animations.advanced.RotationAnimation;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.handler.EventListener;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.impl.combat.KillAura;
import ru.etc1337.client.modules.impl.combat.killaura.rotation.api.Rotation;

public class ReverseRotation implements QuickImports, EventListener {
    private final Timer resetTimer = Timer.create();
    private boolean resetRotation;

    private Vector2f rotationVector = new Vector2f(0, 0);
    private RotationAnimation interp = new RotationAnimation();

    public ReverseRotation() {
        Client.getEventManager().register(this);
    }

    public Vector2f smoothRotate(Vector2f currentRotation, Vector2f targetRotation) {
        return new Vector2f(
                currentRotation.x + MathHelper.wrapDegrees(targetRotation.x - currentRotation.x) * 0.5F,
                MathHelper.clamp(currentRotation.y + MathHelper.wrapDegrees(targetRotation.y -
                        currentRotation.y), -90, 90) * 0.5F
        );
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (resetRotation) {
                final Vector2f originalRotations = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);

                Vector2f smoothRotate = smoothRotate(rotationVector, originalRotations);

                double gcd = Rotation.getGcd();
                smoothRotate.x -= (float) (smoothRotate.x % gcd);
                smoothRotate.y -= (float) (smoothRotate.y % gcd);

                rotationVector = smoothRotate;

                long delay = 220; // желательно сделать формулу, чтобы delay зависил от smoothFactor
                if (resetTimer.finished(delay)) {
                    rotationVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
                    Client.getInstance().getModuleManager().get(KillAura.class).setRotationVector(rotationVector);
                    resetRotation = false;
                    resetTimer.reset();
                }
            }
        }
        if (resetRotation) {
            Player.look(event, rotationVector, Player.Correction.STRICT, null);
        }
    }

    public void resetRotation(Vector2f rotation) {
       /* rotationVector = rotation;
        resetRotation = true;
        resetTimer.reset();*/
        /*if (rotation.is("Snap-Back")) */{
            mc.player.rotationYaw = rotation.x;
            mc.player.rotationPitch = rotation.y;
        }
    }
}