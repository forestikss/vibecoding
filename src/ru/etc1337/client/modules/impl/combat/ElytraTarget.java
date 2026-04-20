package ru.etc1337.client.modules.impl.combat;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.draggable.Draggable;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventAttack;
import ru.etc1337.api.events.impl.game.EventTick;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.input.EventInputKey;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.game.Server;
import ru.etc1337.api.settings.impl.BindSetting;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.combat.killaura.rotation.api.Rotation;
import ru.etc1337.client.modules.impl.movement.SuperFirework;
import ru.etc1337.client.modules.impl.render.ui.api.Header;

@Getter
@ModuleInfo(name = "Elytra Target", description = "Таргет на элитрах", category = ModuleCategory.MOVEMENT)
public class ElytraTarget extends Module {
    private final BooleanSetting prediction = new BooleanSetting("Перелет", this);
    //private final BooleanSetting reallyWorld = new BooleanSetting("ReallyWorld", this);
    public final ModeSetting mode = new ModeSetting("Prediction Mode", this, "Auto", "Default");
    private final SliderSetting predictionScale = new SliderSetting("Prediction", this, 2.6f, 1.5f, 5f, 0.1f).setVisible(() -> mode.is("Default"));
    private final SliderSetting predictionScaleMax = new SliderSetting("Prediction Max", this, 2.6f, 1.5f, 5f, 0.1f).setVisible(() -> mode.is("Auto"));

    public boolean status = true;
    public boolean disableForward = false;
    private final Timer hurtTimer = Timer.create();

    private double bps;
    //private double prevBps;
    public double scale;

    @Override
    public void onEvent(Event event) {
        KillAura killAura = Client.getInstance().getHit().getKillAura();

        if (event instanceof EventUpdate eventUpdate) {
            if (killAura.getTarget() != null && shouldTarget(killAura.getTarget())) {
                double newBps = Player.getBps(killAura.getTarget());
                //if (newBps == 0) {
                  //  bps = prevBps;
                //} else {
                    bps = newBps;
                //    prevBps = newBps;
              //  }
                scale = Math.sqrt(bps) / 2F;
                scale = Math.min(scale, predictionScaleMax.getValue());
            } else {
                bps = 0;
                // prevBps = 0;
            }
            status = prediction.isEnabled();
            if (killAura.getTarget() == null) {
                disableForward = false;
                return;
            }
            if (mc.player.hurtTime > 0 && !killAura.getTarget().lastSwing.finished(500)) {
                disableForward = true;
                hurtTimer.reset();
            }
            if (hurtTimer.finished(500)) disableForward = false;
        }
    }

    public double getPrediction(Entity target) {
        double scale = predictionScale.getValue();
        if (mode.is("Auto")) {
            scale = this.scale;
        }
        return scale;
    }

    public boolean shouldTarget(LivingEntity livingEntity) {
        if (!this.isEnabled() || livingEntity == null || disableForward) return false;
        boolean isTargetValid = livingEntity.isElytraFlying();
        return status && mc.player.isElytraFlying() && isTargetValid;
    }
}