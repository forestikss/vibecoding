package ru.etc1337.client.modules.impl.movement;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.game.Move;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.combat.KillAura;
import ru.etc1337.client.modules.impl.combat.TargetStrafe;

@Getter
@ModuleInfo(name = "Speed", description = "Позволяет Вам бегать с высокой скоростью",category = ModuleCategory.MOVEMENT)
public class Speed extends Module {
    private final ModeSetting mode = new ModeSetting("Тип мода",this,"EntityCollision", "EntityDistance", "Custom");

    private final BooleanSetting onlyPlayers = new BooleanSetting("Бустить, если игрок", this).setVisible(() -> mode.is("EntityCollision"));
    public final SliderSetting speed = new SliderSetting("Скорость буста", this, 8, 1, 8, 1).setVisible(() ->
            (mode.is("EntityCollision") ||
            mode.is("EntityDistance")));

    public final SliderSetting distance = new SliderSetting("Дистанция для буста", this, 3, 0.5f, 5, 0.1F).setVisible(() ->
            mode.is("EntityDistance"));
    public final SliderSetting radius = new SliderSetting("Радиус", this, 1, 0.5f, 1.5f, 0.1F).setVisible(() ->
            mode.is("EntityCollision"));


    private final SliderSetting strafeSpeed = new SliderSetting("Скорость", this, 0.23f, 0.1f, 1.0f, 0.01f).setVisible(() -> mode.is("Custom"));
    private final SliderSetting strafeSpeedLiquid = new SliderSetting("Скорость в жидкости", this, 0.23f, 0.1f, 1.0f, 0.01f).setVisible(() -> mode.is("Custom"));
    private final SliderSetting strafeSpeedGround = new SliderSetting("Скорость на земле", this, 0.23f, 0.1f, 1.0f, 0.01f).setVisible(() -> mode.is("Custom"));
    private final SliderSetting strafeSpeedGroundSpace = new SliderSetting("Скорость на земле с пробелом", this, 0.23f, 0.1f, 2.0f, 0.01f).setVisible(() -> mode.is("Custom"));
    private final BooleanSetting isDamageBoostEnabled = new BooleanSetting("Буст при ударе", this).setVisible(() -> mode.is("Custom"));
    private final SliderSetting boostSpeed = new SliderSetting("Скорость буста", this, 0.5f, 0.1f, 1.5f, 0.1f).setVisible(() -> mode.is("Custom") && this.isDamageBoostEnabled.isEnabled());

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate eventUpdate) {
            if (mc.player == null || mc.world == null) return;
            KillAura killAuraModule = Client.getInstance().getModuleManager().get(KillAura.class);
            TargetStrafe targetStrafe = Client.getInstance().getModuleManager().get(TargetStrafe.class);
            if (mode.is("Custom")) {
                if (targetStrafe.isEnabled() && killAuraModule.getTarget() != null || mc.player.isElytraFlying()) return;
                float f3 = mc.player.isInLava() || mc.player.isInWater() ?
                        strafeSpeedLiquid.getValue() :
                        mc.player.isOnGround() ? (mc.gameSettings.keyBindJump.isKeyDown() ? strafeSpeedGroundSpace.getValue() : this.strafeSpeedGround.getValue()) : this.strafeSpeed.getValue();
                if (this.isDamageBoostEnabled.isEnabled() && TargetStrafe.mc.player.hurtTime > 0 && TargetStrafe.mc.player.isAlive()) {
                    f3 += this.boostSpeed.getValue();
                }


                Move.setSpeed(f3);
            }

            if (mode.is("EntityDistance")) {
                for (PlayerEntity ent : Lists.newArrayList(mc.world.getPlayers())) {
                    if (ent != mc.player && mc.player.getDistanceSq(ent) <= distance.getValue()) {
                        float p = mc.world.getBlockState(mc.player.getPosition()).getBlock().getSlipperiness();
                        float f = mc.player.isOnGround() ? p * 0.91f : 0.91f;
                        float f2 = mc.player.isOnGround() ? p : 0.99f;
                        float yaw = killAuraModule.getTarget() == null ? mc.player.rotationYaw : killAuraModule.getRotationVector().x;
                        double[] motion = Move.forward((speed.getValue() * 0.01) * f * f2, yaw);
                        mc.player.addVelocity(motion[0], 0.0, motion[1]);
                        break;
                    }
                }
            }

            if ((mode.is("EntityCollision")) && Move.isMoving()) {
                int collisions = 0;
                for (Entity ent : mc.world.getAllEntities()) {
                    if (!(ent instanceof PlayerEntity) && onlyPlayers.isEnabled()) continue;

                    if (ent != mc.player && (ent instanceof LivingEntity || ent instanceof BoatEntity) && mc.player.getBoundingBox().expand(new Vector3d(radius.getValue(), radius.getValue(), radius.getValue())).intersects(ent.getBoundingBox()))
                        collisions++;
                }

                float yaw = killAuraModule.getTarget() == null ? mc.player.rotationYaw : killAuraModule.getRotationVector().x;
                double[] motion = Move.forward(((int)speed.getValue() * 0.01) * collisions, yaw);
                mc.player.addVelocity(motion[0], 0.0, motion[1]);
            }       
        }
    }
}
