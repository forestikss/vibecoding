package ru.etc1337.client.modules.impl.player;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.OreBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

import java.util.Comparator;

@ModuleInfo(name = "Nuker", description = "Ломает блоки в радиусе", category = ModuleCategory.PLAYER)
public class Nuker extends Module {
    private SliderSetting radiusXZ = new SliderSetting("Горизонтальный радиус", this, 3, 1, 5, 1);
    private SliderSetting radiusY = new SliderSetting("Вертикальный радиус", this, 1, 1, 6, 1);
    private BooleanSetting priority = new BooleanSetting("Приоритет руд", this);
    private BlockPos pos;
    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            updateNuker();
        }
    }

    private void updateNuker() {
        pos = Player.getCube(mc.player.getPosition(), radiusXZ.getValue(), radiusY.getValue()).stream()
                .filter(this::validBlock)
                .sorted(Comparator.comparing(pos -> mc.player.getDistanceSq(Vector3d.copyCentered(pos))))
                .min(Comparator.comparing(pos -> priority.isEnabled() && mc.world.getBlockState(pos).getBlock() instanceof OreBlock ? 0 : 1))
                .orElse(null);

        if (pos != null) {
            mc.playerController.onPlayerDamageBlock(pos, Direction.UP);
            mc.player.swingArm(Hand.MAIN_HAND);
        }
    }

    private boolean validBlock(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        return !state.isAir() && state.getBlock() != Blocks.CAVE_AIR && state.getBlock() != Blocks.WATER && state.getBlock() != Blocks.LAVA && state.getBlock() != Blocks.BEDROCK && state.getBlock() != Blocks.BARRIER;
    }
}
