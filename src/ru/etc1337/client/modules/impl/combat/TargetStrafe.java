package ru.etc1337.client.modules.impl.combat;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventMotion;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.game.Move;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Target Strafe", description = "Кружит вокруг противника и не оставляет ему шансов на выживание", category = ModuleCategory.COMBAT)
public class TargetStrafe extends Module {
    public final ModeSetting mode = new ModeSetting("Обход", this, "Default", "Collision");
    public final SliderSetting speed = new SliderSetting("Скорость", this,
            0.05f, 0.01f, 0.1f, 0.01F).setVisible(() -> mode.is("Collision"));

    public final SliderSetting distance = new SliderSetting("Дистанция", this,
            2.75F, 1, 3, 0.01F).setVisible(() -> mode.is("Collision"));

    private final BooleanSetting autoJump = new BooleanSetting("Авто Прыжок", this).setVisible(() -> mode.is("Default"));

    private final SliderSetting strafeDistance = new SliderSetting("Дистанция", this, 2.4f, 0.1f, 6.0f, 0.1f).setVisible(() -> mode.is("Default"));
    private final SliderSetting strafeSpeed = new SliderSetting("Скорость", this, 0.23f, 0.1f, 1.0f, 0.01f).setVisible(() -> mode.is("Default"));
    private final BooleanSetting isDamageBoostEnabled = new BooleanSetting("Буст при ударе", this).setVisible(() -> mode.is("Default"));
    private final SliderSetting boostSpeed = new SliderSetting("Скорость буста", this, 0.5f, 0.1f, 1.5f, 0.1f).setVisible(() -> mode.is("Default") && isDamageBoostEnabled.isEnabled());


    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (mode.is("Collision")) {
                if (!Move.isMoving()) return;
                KillAura killAuraModule = Client.getInstance().getModuleManager().get(KillAura.class);
                LivingEntity target = killAuraModule.getTarget();

                if (target == null || mc.player.getDistance(target) >= distance.getValue()) {
                    return;
                }

                Vector3d playerPosition = mc.player.getPositionVec();
                Vector3d targetPosition = target.getPositionVec();

                Vector3d direction = targetPosition.subtract(playerPosition).normalize();

                double currentMotionY = mc.player.getMotion().y;

                float groundSpeed = speed.getValue();
                float fallingSpeed = speed.getValue();
                float jumpSpeed = speed.getValue();

                double speedValue = mc.player.isOnGround() ? groundSpeed : (mc.player.fallDistance > 0 ? fallingSpeed : jumpSpeed);
                double newVelocityX = direction.x * speedValue;
                double newVelocityZ = direction.z * speedValue;

                mc.player.setMotion(
                        mc.player.getMotion().x + newVelocityX,
                        currentMotionY,
                        mc.player.getMotion().z + newVelocityZ
                );
            } else if (mode.is("Default")) {
                KillAura killAura = Client.getInstance().getModuleManager().get(KillAura.class);
                float f;
                LivingEntity entityLivingBase = killAura.getTarget();
                if (entityLivingBase == null || !entityLivingBase.isAlive()) {
                    strafesCheck = false;
                    return;
                }
                if (autoJump.isEnabled() && TargetStrafe.mc.player.isOnGround()) {
                    TargetStrafe.mc.gameSettings.keyBindJump.setPressed(false);
                    TargetStrafe.mc.player.jump();
                    return;
                }
                float f2 = killAura.getMaxDistance() + killAura.getMaxPreDistance().getValue();
                double d2 = TargetStrafe.mc.player.getDistance(entityLivingBase);
                if (d2 > (double) f2) {
                    strafesCheck = false;
                    return;
                }
                TargetStrafe.mc.gameSettings.keyBindForward.setPressed(false);
                float f3 = this.strafeSpeed.getValue();
                if (this.isDamageBoostEnabled.isEnabled() && TargetStrafe.mc.player.hurtTime > 0 && TargetStrafe.mc.player.isAlive()) {
                    f3 += this.boostSpeed.getValue();
                }
                float f4 = (float) P(d2, 0.01f, f2);
                double d3 = (float) Math.atan2(TargetStrafe.mc.player.getPosZ() - entityLivingBase.getPosZ(), TargetStrafe.mc.player.getPosX() - entityLivingBase.getPosX());
                float f5 = C(f3 / f4, 0.01f, 1.0f);
                float f6 = (float) (entityLivingBase.getPosX() + (double) this.strafeDistance.getValue() * Math.cos(d3 += this.isStrafeDirectionClockwise ? (double) f5 : (double) (-f5)));
                if (this.isUnsafeAreaAround(f6, f = (float) (entityLivingBase.getPosZ() + (double) this.strafeDistance.getValue() * Math.sin(d3)))) {
                    this.isStrafeDirectionClockwise = !this.isStrafeDirectionClockwise;
                    f6 = (float) (entityLivingBase.getPosX() + (double) this.strafeDistance.getValue() * Math.cos(d3 += (double) (2.0f * (this.isStrafeDirectionClockwise ? f5 : -f5))));
                    f = (float) (entityLivingBase.getPosX() + (double) this.strafeDistance.getValue() * Math.sin(d3));
                }
                strafesCheck = true;
                double d4 = (double) f3 * -Math.sin((float) Math.toRadians(this.calculateAngleToTarget(f6, f)));
                double d5 = (double) f3 * Math.cos((float) Math.toRadians(this.calculateAngleToTarget(f6, f)));
                if (Double.isNaN(d4) || Double.isNaN(d5)) {
                    return;
                }
                TargetStrafe.mc.player.getMotion().x = d4;
                TargetStrafe.mc.player.getMotion().z = d5;
            }
        }
    }

    public static boolean strafesCheck;
    private boolean isStrafeDirectionClockwise = true;

    public static float C(float f, float f2, float f3) {
        if (f < f2) {
            return f2;
        }
        return f > f3 ? f3 : f;
    }

    public static double P(double d, double d2, double d3) {
        if (d < d2) {
            return d2;
        }
        return d > d3 ? d3 : d;
    }
    
    private boolean isBlockAirBelowPlayer() {
        for (int j = (int)TargetStrafe.mc.player.getPosY(); j > 0; --j) {
            if (Player.getBlock(new BlockPos(TargetStrafe.mc.player.getPosX(), j, TargetStrafe.mc.player.getPosZ())) instanceof AirBlock) continue;
            return false;
        }
        return true;
    }

    public boolean isUnsafeAreaAround(double d2, double d3) {
        if (TargetStrafe.mc.player.collidedHorizontally || TargetStrafe.mc.gameSettings.keyBindLeft.isKeyDown() || TargetStrafe.mc.gameSettings.keyBindRight.isKeyDown()) {
            return true;
        }
        for (int j = (int)(TargetStrafe.mc.player.getPosY() + 4.0); j >= 0; --j) {
            BlockPos blockPos = new BlockPos(d2, j, d3);
            if (TargetStrafe.mc.world.getBlockState(blockPos).getBlock().equals(Blocks.LAVA) || TargetStrafe.mc.world.getBlockState(blockPos).getBlock().equals(Blocks.FIRE)) {
                return true;
            }
            if (Player.getBlock(blockPos) == Blocks.COBWEB) {
                return true;
            }
            if (this.isBlockAirBelowPlayer()) {
                return true;
            }
            if (TargetStrafe.mc.world.isAirBlock(blockPos)) continue;
            return false;
        }
        return true;
    }

    private float calculateAngleToTarget(float p1, float p2) {
        double d3 = (double)p1 - TargetStrafe.mc.player.getPosX();
        double d5 = (double)p2 - TargetStrafe.mc.player.getPosZ();
        return (float)(Math.atan2(d5, d3) * 180.0 / Math.PI - 90.0);
    }
}
