package ru.etc1337.client.modules.impl.render;

import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SUpdateTimePacket;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.packet.EventReceivePacket;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Custom Time", description = "Пресеты времени суток", category = ModuleCategory.RENDER)
public class CustomTime extends Module {

    // Minecraft time values:
    // 0     = рассвет
    // 6000  = полдень
    // 12000 = закат
    // 13000 = ночь
    // 18000 = полночь
    private final ModeSetting timeMode = new ModeSetting("Время", this,
            "Утро", "День", "Закат", "Ночь");

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventReceivePacket e) {
            if (e.getPacket() instanceof SUpdateTimePacket) {
                e.setCancelled(true);
            } else if (e.getPacket() instanceof SChangeGameStatePacket wrapper) {
                if (wrapper.getState() == SChangeGameStatePacket.field_241765_b_
                        || wrapper.getState() == SChangeGameStatePacket.field_241766_c_) {
                    e.setCancelled(true);
                }
            }
        }

        if (event instanceof EventUpdate && mc.world != null) {
            mc.world.setDayTime(getTime());
        }
    }

    private long getTime() {
        return switch (timeMode.getCurrentMode()) {
            case "Утро"   -> 0L;
            case "День"   -> 6000L;
            case "Закат"  -> 12000L;
            case "Ночь"   -> 18000L;
            default       -> 6000L;
        };
    }
}
