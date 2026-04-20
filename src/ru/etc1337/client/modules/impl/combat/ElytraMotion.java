
package ru.etc1337.client.modules.impl.combat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector2f;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.draggable.Draggable;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventMove;
import ru.etc1337.api.events.impl.game.EventUpdate;

import ru.etc1337.api.game.Chat;
import ru.etc1337.api.game.Inventory;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.movement.SuperFirework;
import ru.kotopushka.compiler.sdk.annotations.Compile;
;

@ModuleInfo(name = "Elytra Motion", description = "Дополнение для Элитра-Таргета", category = ModuleCategory.MOVEMENT)
public class ElytraMotion extends Module {
    public final SliderSetting attackDistance = new SliderSetting("Дистанция работы", this, 3.0F, 0.1F, 5.0F, 0.01F);

    public boolean freeze;
    public Vector2f direction = Vector2f.ZERO;
    private final BooleanSetting autoFirework = new BooleanSetting("Auto Firework", this);
    private final BooleanSetting bypass = new BooleanSetting("Matrix Bypass", this);
    private final Timer timer = new Timer();

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate eventUpdate) {
            if (!mc.player.isElytraFlying()) {
                freeze = false;
                return;
            }
            KillAura killAura = Client.getInstance().getModuleManager().get(KillAura.class);
            ElytraTarget elytraTarget = Client.getInstance().getModuleManager().get(ElytraTarget.class);

            if (check(killAura, elytraTarget)) {
                mc.gameSettings.keyBindForward.setPressed(false);
                freeze = true;
            } else {
                mc.gameSettings.keyBindForward.setPressed(true);
                freeze = false;
            }

            if (freeze) {
                if (timer.finished(500) && bypass.isEnabled()) {
                    Inventory.Use.use(Items.FIREWORK_ROCKET, false, true);
                    timer.reset();
                }
            } else if (autoFirework.isEnabled()) {
                if (killAura.getTarget() != null && Player.getBps(mc.player) <= 30 && timer.finished(500)) {
                    Inventory.Use.use(Items.FIREWORK_ROCKET, false, true);
                    timer.reset();
                }
            }
        }

        if (event instanceof EventMove eventMove && freeze) {
            eventMove.getMotion().x = 0;
            eventMove.getMotion().y = 0;
            eventMove.getMotion().z = 0;
        }
    }

    @Compile
    public boolean check(KillAura killAura, ElytraTarget elytraTarget) {
        LivingEntity target = killAura.getTarget();
        if (target == null || !mc.player.isElytraFlying()) return false;
        boolean canTarget = elytraTarget.shouldTarget(target);
        return !canTarget && target.getDistance(mc.player) < attackDistance.getValue();
    }

    @Override
    public void onDisable() {
        freeze = false;
        super.onDisable();
    }
}

