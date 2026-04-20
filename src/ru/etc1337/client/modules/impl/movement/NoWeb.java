package ru.etc1337.client.modules.impl.movement;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.game.Move;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;

@ModuleInfo(name = "No Web", description = "Помогает передвигаться в паутине", category = ModuleCategory.MOVEMENT)
public class NoWeb extends Module {

    @Compile
    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventUpdate) || mc.player == null || mc.world == null) {
            return;
        }
        double x, z, y;
        boolean inWeb = false;
        for (x = -0.3; x <= 0.3; x += 0.3) {
            for (z = -0.3; z <= 0.3; z += 0.3) {
                for (y = mc.player.getEyeHeight(); y >= 0.0; y -= 0.1) {
                    if (mc.world.getBlockState(new BlockPos(mc.player.getPosX() + x, mc.player.getPosY() + y, mc.player.getPosZ() + z)).getBlock() == Blocks.COBWEB) {
                        inWeb = true;
                        break;
                    }
                }
                if (mc.world.getBlockState(new BlockPos(mc.player.getPosX() + x, mc.player.getPosY(), mc.player.getPosZ() + z)).getBlock() == Blocks.COBWEB) {
                    inWeb = true;
                    break;
                }
            }
        }

        if (!inWeb && mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY() + mc.player.getEyeHeight() + 1.65, mc.player.getPosZ())).getBlock() == Blocks.COBWEB) {
            inWeb = true;
        }

        if (inWeb) {
            mc.player.getMotion().y = 0.0;
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.player.getMotion().y = 1.15F;
            } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.player.getMotion().y = -1.15F;
            }
            Move.setMotion(0.223F);
        }
    }
}
