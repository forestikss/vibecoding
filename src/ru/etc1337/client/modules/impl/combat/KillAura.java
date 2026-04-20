package ru.etc1337.client.modules.impl.combat;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventTick;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.game.Maths;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.game.RayTrace;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.combat.killaura.TargetFinder;

import ru.etc1337.client.modules.impl.combat.killaura.rotation.*;
import ru.etc1337.client.modules.impl.combat.killaura.rotation.api.Hit;
import ru.etc1337.client.modules.impl.combat.killaura.rotation.api.Rotation;
import ru.kotopushka.compiler.sdk.annotations.Compile;

@ModuleInfo(name = "Kill Aura", description = "Бьет ентити", category = ModuleCategory.COMBAT)
public class KillAura extends Module {
    @Getter
    private LivingEntity target;
    @Getter
    @Setter
    private Vector2f rotationVector = new Vector2f(0, 0);

    public final ModeSetting rotation = new ModeSetting("Rotation", this, "Grim Simple", "Snap",
            "Universal", "Legit", "Client", "Snap-Back");
    public final BooleanSetting yawRandom = new BooleanSetting("Random Yaw", this).setVisible(() -> rotation.is("Client"));

    public final SliderSetting universalSpeedX = new SliderSetting("Rotation Speed Yaw", this,
            0.5f, 0.1f, 2.5F, 0.1F).setVisible(() -> rotation.is("Universal") || rotation.is("Legit") || rotation.is("Client"));
    public final SliderSetting universalSpeedY = new SliderSetting("Rotation Speed Pitch", this,
            1, 1, 2.5f, 0.1F).setVisible(() -> rotation.is("Universal") || rotation.is("Legit"));

    // Main Settings
    private final MultiModeSetting targetType = new MultiModeSetting("Targets", this,
            "Players",
            "Mobs",
            "Animals",
            "Friends");

    private final ModeSetting moveFix = new ModeSetting("Коррекция", this, "Нет", "Свободная", "Сфокусированная", "Полная");

    @Getter
    private final SliderSetting maxDistance = new SliderSetting("Дистанция", this,
            3.05F, 2.5F, 6.0F, 0.01F);
    @Getter
    private final SliderSetting maxPreDistance = new SliderSetting("Дистанция наводки", this,
            0.3F, 0.0F, 6, 0.1F);

    private final BooleanSetting unpressShield = new BooleanSetting("Отжимать щит", this);
    private final BooleanSetting breakShield = new BooleanSetting("Ломать Щит", this);

    public final BooleanSetting rayTrace = new BooleanSetting("Ray Trace", this);
    public final BooleanSetting wallsCheck = new BooleanSetting("Walls Check", this).setVisible(rayTrace::isEnabled);

    public final BooleanSetting ignoreEat = new BooleanSetting("Ignore Eat", this);


    public final BooleanSetting onlyCriticals = new BooleanSetting("Бить критами", this);
    public final BooleanSetting spaceOnly = new BooleanSetting("Умные криты", this).setVisible(() -> onlyCriticals.isEnabled());
    public final BooleanSetting randomFd = new BooleanSetting("Рандомный Fall Distance", this).setVisible(() -> onlyCriticals.isEnabled());
    public final BooleanSetting legitSprint = new BooleanSetting("Легитный спринт", this).setVisible(() -> onlyCriticals.isEnabled());


    private float snapTicks;
    private boolean hasResetRotation = true;
    private boolean hasTarget = false;
    public boolean lookingAtTarget = false;
    private boolean shield;

    public boolean isFalling = false;

    @Getter float randomFactor;
    @Getter private int fdCount;
    public final Timer attackTimer = new Timer();
    final Timer disableTimer = new Timer();
    public boolean hitTick = false;


    @Getter
    private final ElytraRotation elytraRotation = new ElytraRotation();

    @Getter
    private final Rotation simpleRotation = new SimpleRotation(),
            universalRotation = new UniversalRotation(),
            legitRotation = new LegitRotation(), aimbotRotation = new AimbotRotation();
    @Override
    public void onEvent(Event event) {
        final ElytraTarget elytraTarget = Client.getInstance().getModuleManager().get(ElytraTarget.class);

        rotation().onEvent(event);
        if (event instanceof EventTick) {
            if (mc.player == null || mc.world == null) {
                isFalling = false;
                return;
            }
            isFalling = Hit.INSTANCE.isValidFallState();
        }
        if (event instanceof EventUpdate) {
            target = updateTarget();
            if (target != null) {
                updateAttack();
                hasResetRotation = false;
                hasTarget = true;
            } else {
                if (!hasResetRotation) {
                    if (shouldReverseRotate()) {
                        Client.getInstance().getReverseRotation().resetRotation(this.rotationVector);
                    }
                    this.rotationVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
                    lookingAtTarget = false;
                    hasResetRotation = true;
                }
                hasTarget = false;
            }
        }

/*        if (event instanceof EventRender2D eventRender2D) {
            if (target == null || elytraRotation.position == Vector3d.ZERO) return;
            Vector3d point = elytraRotation.position;
            Vector2f screenPos = Render.project(point.x, point.y, point.z);
            if (screenPos.x == Float.MAX_VALUE || screenPos.y == Float.MAX_VALUE) return;

            float stackSize = 20;
            float x = screenPos.x - (stackSize / 2F);
            float y = screenPos.y - (stackSize / 2F);
            Render.drawRect(eventRender2D.getMatrixStack(), x, y, stackSize, stackSize, FixColor.GREEN.alpha(255));
        }*/

        if (mc.player != null && mc.world != null && target != null && !rotation().equals(aimbotRotation)) {
            Player.Correction correction = Player.Correction.STRICT;
            if (moveFix.is("Нет")) correction = Player.Correction.NONE;
            if (moveFix.is("Свободная")) correction = Player.Correction.SILENT;
            if (moveFix.is("Сфокусированная")) correction = Player.Correction.STRICT;
            if (moveFix.is("Полная")) correction = Player.Correction.FULL;
            Player.look(event, rotationVector, correction, target);
        }
    }

    private void updateAttack() {
        final Hit hit = Hit.INSTANCE;
        boolean isFalling = hit.canHit(onlyCriticals.isEnabled(), spaceOnly.isEnabled(), this.isFalling);
        boolean isElytraFlying = mc.player.isElytraFlying();

        if ((rotation.is("Snap") || rotation.is("Snap-Back")) && !isElytraFlying) {
            if (isFalling) {
                attackTarget(hit);
                snapTicks = 2;
            }

            if (snapTicks > 0 && this.target.getDistance(mc.player) < this.getMaxDistance()) {
                updateRotations(false);
                snapTicks--;
            } else {
                if (!rotation.is("Snap-Back")) {
                    this.rotationVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
                }
            }
        } else if ((!rotation.is("Snap") || !rotation.is("Snap-Back")) || isElytraFlying) {
            if (isFalling) {
                attackTarget(hit);
            }
            updateRotations(false);
        }
    }


    private void updateRotations(boolean eventTick) {
        rotation().update(target);

        rotationVector = rotation().rotation;

        Vector2f rot = rotation.is("Client") ? new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch) : rotationVector;
        if (ignoreEat.isEnabled() && mc.player.isHandActive() && mc.player.getActiveItemStack().isFood()) {
            lookingAtTarget = false;
        } else {
            lookingAtTarget = mc.player.isElytraFlying() || !this.rayTrace.isEnabled()
                    || (RayTrace.rayTraceWithBlock(getMaxDistance(), rot.x, rot.y, mc.player, target) && (!wallsCheck.isEnabled() ||
                    mc.pointedEntity != null /*&& mc.pointedEntity.equals(target))*/));
        }
    }

    private Rotation rotation() {
        final ElytraTarget elytraTarget = Client.getInstance().getModuleManager().get(ElytraTarget.class);
        if (mc.player != null && elytraTarget.shouldTarget(target)) return elytraRotation;
        if (rotation.is("Universal")) return universalRotation;
        if (rotation.is("Legit")) return legitRotation;
        if (rotation.is("Client")) return aimbotRotation;
        return simpleRotation;
    }

    private boolean canHit(Hit hit) {
        if (!lookingAtTarget) return false;
        if (!distanceCheck()) return false;
        return hit.preHit(target, legitSprint.isEnabled() ? Hit.SprintReset.LEGIT : Hit.SprintReset.PACKET, breakShield.isEnabled());
    }

    public boolean distanceCheck() {
        if (target == null) return false;
        final ElytraTarget elytraTarget = Client.getInstance().getModuleManager().get(ElytraTarget.class);
        if (elytraTarget.shouldTarget(target)) {
            Vector3d targetVec = elytraRotation.getTargetVec();
            if (targetVec == null) return false;

            final double currentDistance = mc.player.getPositionVec().distanceTo(targetVec);
            boolean canAttack = currentDistance <= (elytraTarget.getPrediction(target) - 0.5F) || mc.player.getBoundingBox().intersects(AxisAlignedBB.fromVector(targetVec));
            if (!canAttack) {
                return false;
            }
        } else {
            if (target.getDistance(mc.player) > getMaxDistance()) {
                return false;
            }
        }
        return true;
    }

    private void attackTarget(Hit hit) {
        if (!canHit(hit)) return;
        hitTick = true;
        updateRotations(true);

        attackTimer.reset();

        shield = mc.player.isHandActive() && mc.player.getActiveItemStack().getItem().getUseAction(mc.player.getActiveItemStack()) == UseAction.BLOCK && unpressShield.isEnabled();
        if (shield) {
            mc.playerController.onStoppedUsingItem(mc.player);
        }

        hit.upHitCooldown();


        mc.playerController.attackEntity(mc.player, this.target);
        mc.player.swingArm(Hand.MAIN_HAND);

        hitTick = false;
        hit.postHit();
        rotation().attacked();
        randomFactor = Maths.random(0, 1);
        if (mc.player.fallDistance > 0.1f) {
            fdCount = 0;
        } else {
            fdCount++;
        }
    }

    private float getPreDistance() {
        return Client.getInstance().getModuleManager().get(ElytraTarget.class).isEnabled() && mc.player.isElytraFlying() ? 64 : maxPreDistance.getValue();
    }

    private LivingEntity updateTarget() {
        TargetFinder.EntityFilter filter = new TargetFinder.EntityFilter(targetType);
        if (mc.world != null) TargetFinder.searchTargets(mc.world.getAllEntities(), getMaxDistance() + this.getPreDistance());
        TargetFinder.validateTarget(filter::isValid);
        return TargetFinder.currentTarget;
    }

    public float getMaxDistance() {
        return maxDistance.getValue();
    }

    @Override
    public void onEnable() {
        lookingAtTarget = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        disableTimer.reset();
        lookingAtTarget = false;
        isFalling = false;
        hitTick = false;
        if (hasTarget) {
            if (shouldReverseRotate()) {
                Client.getInstance().getReverseRotation().resetRotation(this.rotationVector);
                this.rotationVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
            }
        }

        TargetFinder.releaseTarget();
        target = null;

        super.onDisable();
    }

    private boolean shouldReverseRotate() {
        return rotation.is("Snap-Back");
    }
}