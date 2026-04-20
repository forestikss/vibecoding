package ru.etc1337.client.modules.impl.render;

import lombok.Getter;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.optifine.Config;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.game.EventWorldColor;
import ru.etc1337.api.events.impl.packet.EventReceivePacket;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@Getter
@ModuleInfo(name = "Custom World", description = "Настройка мира", category = ModuleCategory.RENDER)
public class CustomWorld extends Module {
    public MultiModeSetting elements = new MultiModeSetting("Выбор", this, "Время", "Туман");
    private final SliderSetting distance = new SliderSetting("Дистанция тумана", this, 1F, 0F, 1F, 0.01F).setVisible(() -> elements.get("Туман").isEnabled());
    public SliderSetting time = new SliderSetting("Время", this, 16000, 0, 24000, 100).setVisible(() -> elements.get("Время").isEnabled());

    @Override
    public void onEvent(Event event) {
        if (elements.get("Время").isEnabled()) {
            if (event instanceof EventReceivePacket eventReceivePacket) {
                if (eventReceivePacket.getPacket() instanceof SUpdateTimePacket) {
                    event.setCancelled(true);
                } else if (eventReceivePacket.getPacket() instanceof SChangeGameStatePacket wrapper) {
                    if (wrapper.getState() == SChangeGameStatePacket.field_241765_b_ || wrapper.getState() == SChangeGameStatePacket.field_241766_c_) {
                        event.setCancelled(true);
                    }
                }
            }
            if (event instanceof EventUpdate) {
                mc.world.setDayTime((long) time.getValue());
            }
            if (event instanceof EventWorldColor e) {
                if (!elements.get("Туман").isEnabled()) return;
                float[] rgb = FixColor.getRGBAf(TempColor.getClientColor().getRGB());
                e.setRed(rgb[0]);
                e.setGreen(rgb[1]);
                e.setBlue(rgb[2]);
                if (Config.isFancyFogAvailable()) {
                    if (mc.gameSettings.ofFogType != 2) {
                        mc.gameSettings.ofFogType = 2;
                    }
                } else {
                    if (mc.gameSettings.ofFogType != 2 && mc.gameSettings.ofFogType != 1) {
                        mc.gameSettings.ofFogType = 1;
                    }
                }
            }
        }
    }
}
