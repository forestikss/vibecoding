/*
package ru.etc1337.client.modules.impl.misc;

import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.server.SSendResourcePackPacket;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventTick;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.packet.EventReceivePacket;
import ru.etc1337.api.settings.impl.ColorSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Theme", description = "Отключает ресурс пак сервера", category = ModuleCategory.MISC)
public class Test extends Module {
    public final ColorSetting clientColor = new ColorSetting("Client Color", this,
            new FixColor(255, 255, 255,255).getRGB());

    public final ColorSetting backgroundColor = new ColorSetting("Background Color", this,
            new FixColor(255, 255, 255,255).getRGB());

    public final ColorSetting fontColor = new ColorSetting("Font Color", this,
            new FixColor(255, 255, 255,255).getRGB());

    TempColor.Theme theme = TempColor.Theme.DARK;
    
    @Override
    public void onEvent(Event event) {

    }

    @Override
    public void onEnable() {
        theme = TempColor.getCurrentTheme();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        TempColor.setTheme(theme);
        super.onDisable();
    }
}
*/
