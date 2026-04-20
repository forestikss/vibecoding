package ru.etc1337.client.modules.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;

@ModuleInfo(name = "Hit Boxes", description = "Увеличивает оффсет игрока", category = ModuleCategory.COMBAT)
public class HitBoxes extends Module {
    private SliderSetting size = new SliderSetting("Размер", this, 0.2F, 0F, 3F, 0.1F);

    @Compile
    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            adjustForPlayer();
        }
    }

    @Compile
    private void adjustForPlayer() {
        if (mc.player == null || mc.world == null) return;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (skip(player)) {
                continue;
            }
            float sizeMultiplier = this.size.getValue() * 2.5F;
            setBox(player, sizeMultiplier);
        }
    }

    @Compile
    private boolean skip(PlayerEntity player) {
        return player == mc.player || !player.isAlive();
    }

    @Compile
    private void setBox(Entity entity, float size) {
        AxisAlignedBB newBoundingBox = calc(entity, size);
        entity.setBoundingBox(newBoundingBox);
    }

    @Compile
    private AxisAlignedBB calc(Entity entity, float size) {
        double minX = entity.getPosX() - size;
        double minY = entity.getBoundingBox().minY;
        double minZ = entity.getPosZ() - size;
        double maxX = entity.getPosX() + size;
        double maxY = entity.getBoundingBox().maxY;
        double maxZ = entity.getPosZ() + size;
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
