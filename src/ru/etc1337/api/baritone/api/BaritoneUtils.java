package ru.etc1337.api.baritone.api;

import lombok.experimental.UtilityClass;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.loot.LootPredicateManager;
import net.minecraft.loot.LootTableManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import ru.etc1337.api.baritone.api.event.events.RotationMoveEvent;
import ru.etc1337.api.baritone.api.event.events.SprintStateEvent;
import ru.etc1337.api.baritone.api.utils.BlockOptionalMeta;

import java.util.Optional;

@UtilityClass
public class BaritoneUtils {

    public boolean isAllowFlying(PlayerAbilities capabilities, ClientPlayerEntity clientPlayer) {
        IBaritone baritone = BaritoneAPI.getProvider().getBaritoneForPlayer(clientPlayer);
        if (baritone == null) {
            return capabilities.allowFlying;
        }
        return !baritone.getPathingBehavior().isPathing() && capabilities.allowFlying;
    }

    public boolean isKeyDown(KeyBinding keyBinding, ClientPlayerEntity clientPlayer) {
        IBaritone baritone = BaritoneAPI.getProvider().getBaritoneForPlayer(clientPlayer);
        if (baritone == null) {
            return keyBinding.isKeyDown();
        }
        if (BaritoneUtils.isEnabled()) {
            SprintStateEvent event = new SprintStateEvent();
            baritone.getGameEventHandler().onPlayerSprintState(event);
            if (event.getState() != null) {
                return event.getState();
            }
        }
        if (baritone != BaritoneAPI.getProvider().getPrimaryBaritone()) {
            return false;
        }
        return keyBinding.isKeyDown();
    }

    public float overrideYaw(LivingEntity self, RotationMoveEvent jumpRotationEvent) {
        if (getBaritone(self).isPresent() && jumpRotationEvent != null) {
            return jumpRotationEvent.getYaw();
        }
        return self.rotationYaw;
    }

    public MinecraftServer getServer(ServerWorld world) {
        if (world == null) {
            return null;
        }
        return world.getServer();
    }

    public LootTableManager getLootTableManager(MinecraftServer server) {
        if (server == null) {
            return BlockOptionalMeta.getManager();
        }
        return server.getLootTableManager();
    }

    public LootPredicateManager getLootPredicateManager(MinecraftServer server) {
        if (server == null) {
            return BlockOptionalMeta.getPredicateManager();
        }
        return server.func_229736_aP_();
    }

    public boolean passEvents(Screen screen, ClientPlayerEntity player) {
        return (BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing() && player != null) || screen.passEvents;
    }

    public boolean isEnabled() {
        return true;
    }

    public Optional<IBaritone> getBaritone(Object object) {
        if (object instanceof ClientPlayerEntity clientPlayer) {
            return Optional.ofNullable(BaritoneAPI.getProvider().getBaritoneForPlayer(clientPlayer));
        } else {
            return Optional.empty();
        }
    }
}
