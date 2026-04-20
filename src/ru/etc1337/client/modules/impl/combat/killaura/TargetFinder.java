package ru.etc1337.client.modules.impl.combat.killaura;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import ru.etc1337.Client;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.client.modules.impl.combat.AntiBot;
import ru.etc1337.client.modules.impl.player.FreeCam;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@UtilityClass
public class TargetFinder implements QuickImports {
    public LivingEntity currentTarget = null;
    public Stream<LivingEntity> potentialTargets;

    public void lockTarget(LivingEntity target) {
        if (currentTarget == null) {
            currentTarget = target;
        }
    }

    public void releaseTarget() {
        currentTarget = null;
    }

    public void validateTarget(Predicate<LivingEntity> predicate) {
        findFirstMatch(predicate).ifPresent(TargetFinder::lockTarget);

        if (currentTarget != null && !predicate.test(currentTarget)) {
            releaseTarget();
        }
    }

    public void searchTargets(Iterable<Entity> entities, float maxDistance) {
        if (isTargetOutOfRange(maxDistance)) {
            releaseTarget();
        }

        potentialTargets = createStreamFromEntities(entities, maxDistance);
    }

    private boolean isTargetOutOfRange(float maxDistance) {
        return currentTarget != null && currentTarget.getDistance(mc.player) > maxDistance;
    }

    private Stream<LivingEntity> createStreamFromEntities(Iterable<Entity> entities, float maxDistance) {
        return StreamSupport.stream(entities.spliterator(), false)
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .filter(entity -> mc.player.getEyePosition(1.0F).distanceTo(entity.getEyePosition(1.0F)) <= maxDistance)
                .sorted(Comparator.comparingDouble(entity -> entity.getDistance(mc.player)));
    }

    private Optional<LivingEntity> findFirstMatch(Predicate<LivingEntity> predicate) {
        return potentialTargets.filter(predicate).findFirst();
    }

    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class EntityFilter {
        MultiModeSetting targetSettings;

        public boolean isValid(LivingEntity entity) {
            if (isLocalPlayer(entity)) return false;
            if (isInvalidHealth(entity)) return false;
            if (isBotPlayer(entity)) return false;

            return isValidEntityType(entity);
        }

        private boolean isLocalPlayer(LivingEntity entity) {
            RemoteClientPlayerEntity fakePlayer = Client.getInstance().getModuleManager().get(FreeCam.class).getEnt();
            return entity == mc.player || fakePlayer != null && entity == fakePlayer;
        }

        private boolean isInvalidHealth(LivingEntity entity) {
            return !entity.isAlive() || entity.getRealHealth() <= 0;
        }

        private boolean isBotPlayer(LivingEntity entity) {
            return entity instanceof PlayerEntity player && AntiBot.isBot(player);
        }

        private boolean isValidEntityType(LivingEntity entity) {
            if (entity instanceof PlayerEntity player) {
                if (!targetSettings.get("Friends").isEnabled() && Client.getInstance().getFriendManager().isFriend(player.getName().getString())) {
                    return false;
                }
                return targetSettings.get("Players").isEnabled();
            }
            if (entity instanceof MobEntity) {
                return targetSettings.get("Mobs").isEnabled();
            }
            return !(entity instanceof ArmorStandEntity);
        }
    }
}