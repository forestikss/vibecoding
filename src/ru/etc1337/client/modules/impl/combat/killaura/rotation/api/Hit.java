package ru.etc1337.client.modules.impl.combat.killaura.rotation.api;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.handler.EventListener;
import ru.etc1337.api.events.impl.game.EventInputMove;
import ru.etc1337.api.events.impl.game.EventMotion;
import ru.etc1337.api.events.impl.game.EventTick;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.game.*;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.impl.combat.Criticals;
import ru.etc1337.client.modules.impl.combat.KillAura;
import ru.etc1337.client.modules.impl.movement.AutoSprint;

import java.util.Optional;

@Getter @Setter
public class Hit implements QuickImports, EventListener {
    public static Hit INSTANCE;

    KillAura killAura = Client.getInstance().getModuleManager().get(KillAura.class);
    AutoSprint autoSprint = Client.getInstance().getModuleManager().get(AutoSprint.class);

    private final Timer hitCooldown = new Timer();

    @Getter
    private boolean wasSprinting = false;
    private SprintReset sprintReset;

    public Hit() {
        Client.getEventManager().register(this);
        INSTANCE = this;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventTick eventTick) {
            if (killAura.legitSprint.isEnabled()) {
                updateSprintStatus();
            }
        }
    }

    private boolean canPlayerSprint() {
        LivingEntity target = killAura.getTarget();

        if (target == null || !killAura.isEnabled() || !killAura.legitSprint.isEnabled()) return true;

        boolean cancelReason = shouldCancelCrit();
        boolean isInDistance = killAura.distanceCheck();

        boolean canHit = killAura.lookingAtTarget && isInDistance && killAura.legitSprint.isEnabled() && hitCooldown.finished(300)
                && mc.player.getCooledAttackStrength(0.5F) >= 0.8F;

    /*    if (canHit && !(Player.getBlock(0, 2, 0) instanceof AirBlock)) {
            return false;
        }
*/
        return !canHit || cancelReason;
    }

    private void updateSprintStatus() {
        if (mc.player == null || mc.world == null) return;

        boolean canSprint = canPlayerSprint();
        autoSprint.setCanSprint(canSprint);
    }

    public boolean shouldCancelCrit() {
        return (Client.getInstance().getModuleManager().get(Criticals.class).isEnabled() && Criticals.canCrit())
                || mc.player.isInWater() && mc.player.areEyesInFluid(FluidTags.WATER)
                || mc.player.isInLava()
                || Player.isInWeb()
                || mc.player.isOnLadder() || mc.player.isPassenger()
                || mc.player.abilities.isFlying
                || mc.player.isPotionActive(Effects.BLINDNESS)
                || mc.player.isPotionActive(Effects.LEVITATION)
                || mc.player.isPotionActive(Effects.SLOW_FALLING);
    }

    public boolean isValidFallState() {
        double yDiff = ((int) mc.player.getPosY()) - mc.player.getPosY();
        return (yDiff == -0.01250004768371582 || yDiff == -0.1875) && !mc.player.movementInput.sneaking ||
                mc.player.fallDistance > getFallDistance() || killAura.getTarget() != null && Player.getBlock(0, 2, 0) != Blocks.AIR && Player.getBlock(0, -1, 0) != Blocks.AIR && Server.is("spooky") && mc.player.isOnGround() && killAura.getFdCount() > 8;
    }

    private boolean isAttackReady(float minus) {
        float attackStrength = mc.player.getCooledAttackStrength(0.5F);
        float attackDelay = 0.9F;
        return hitCooldown.finished(500) && attackStrength > attackDelay;
    }

    private float getFallDistance() {

        if (Server.is("spooky") || Server.isRW() && killAura.randomFd.isEnabled()) {
            float fallDistance = killAura.getTarget() != null && Player.collideWith(killAura.getTarget()) &&
                    !(Player.getBlock(0, 2, 0) != Blocks.AIR && Player.getBlock(0, -1, 0) != Blocks.AIR &&
                            mc.getGameSettings().keyBindJump.isKeyDown()) ? 0.1F : (killAura.getFdCount() >= 10 ? 0.1F : 0.0F);

            return fallDistance;
        } else {
            if (killAura.randomFd.isEnabled()) {
                return Maths.random(0, 0.3f);
            }
        }
        return 0;
    }

    public boolean canHit(boolean onlyCrit, boolean onlySpace, boolean isCrit) {
        if (!isAttackReady(0.0F)) return false;
        if (shouldCancelCrit()) return true;
        if (onlySpace && !mc.gameSettings.keyBindJump.isKeyDown() && mc.player.isOnGround()) return true;

        return !onlyCrit || isCrit;
    }

    private Optional<Integer> findAxeInInventory() {
        for (int i = 0; i < 9; ++i) {
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof AxeItem) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    private boolean tryBreakShield(LivingEntity entity) {
        if (entity instanceof PlayerEntity player && player.isHandActive() && player.getActiveItemStack().getItem() instanceof ShieldItem) {
            Optional<Integer> axeSlot = findAxeInInventory();
            if (axeSlot.isPresent()) {
                int axeIndex = axeSlot.get();
                mc.player.connection.sendPacket(new CHeldItemChangePacket(axeIndex));
                mc.playerController.attackEntity(mc.player, player);
                mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                return true;
            }
        }
        return false;
    }

    public void upHitCooldown() {
        hitCooldown.reset();
    }

    public boolean preHit(LivingEntity entity, SprintReset sprintReset, boolean breakShield) {
        if (breakShield && tryBreakShield(entity)) {
            upHitCooldown();
            return false;
        }

        this.sprintReset = sprintReset;

        if (mc.player.isServerSprintState()) {
            if (sprintReset == SprintReset.PACKET) {
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
                mc.player.setServerSprintState(false);
                mc.player.setSprinting(false);
                wasSprinting = true;
            } else if (sprintReset == SprintReset.LEGIT) {
                killAura.hitTick = true;
                wasSprinting = true;
                return mc.player.isOnGround() && mc.player.isSprinting();
            }
        }

        return true;
    }

    public boolean postHit() {
        if (wasSprinting) {
            if (sprintReset == SprintReset.PACKET) {
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
                mc.player.setServerSprintState(true);
                mc.player.setSprinting(true);
            }
            wasSprinting = false;
        }
        return true;
    }

    public enum SprintReset {
        NONE,
        PACKET,
        LEGIT
    }
}
