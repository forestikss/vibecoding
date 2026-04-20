package ru.etc1337.client.modules.impl.misc;

import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.util.text.TextFormatting;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.game.Chat;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

@ModuleInfo(name = "Death Coordinates", description = "Координаты смерти", category = ModuleCategory.MISC)
public class DeathCoordinates extends Module {

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            checkDeathCoordinates();
        }
    }

    public void checkDeathCoordinates() {
        if (isPlayerDead()) {
            int positionX = mc.player.getPosition().getX();
            int positionY = mc.player.getPosition().getY();
            int positionZ = mc.player.getPosition().getZ();
            if (mc.player.deathTime < 1) {
                printDeathCoordinates(positionX, positionY, positionZ);
            }
        }
    }

    private boolean isPlayerDead() {
        return mc.player.getRealHealth() < 1.0f && mc.currentScreen instanceof DeathScreen;
    }

    @Compile
    @VMProtect(type = VMProtectType.MUTATION)
    private void printDeathCoordinates(int x, int y, int z) {
        String message = TextFormatting.RED + "x: " + TextFormatting.WHITE + x + TextFormatting.RED + " y: " + TextFormatting.WHITE + y + TextFormatting.RED + " z: " + TextFormatting.WHITE + z + TextFormatting.RESET;
        Chat.send(message);
    }
}
