package ru.etc1337.client.modules.impl.player;

import net.minecraft.item.Items;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.input.EventInputKey;
import ru.etc1337.api.game.Inventory;
import ru.etc1337.api.settings.impl.BindSetting;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.combat.KillAura;

@ModuleInfo(name = "Click Pearl", description = "Кидает Эндер-Жемчуг по кнопке", category = ModuleCategory.PLAYER)
public class ClickPearl extends Module {
    private final BindSetting throwKey = new BindSetting("Кнопка Броска", this, -1);
    private final BooleanSetting back = new BooleanSetting("Возращать", this);

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventInputKey eventInputKey) {
            if (eventInputKey.isReleased()) return;
            if (eventInputKey.getKey() == throwKey.getKey()) {
                Inventory.Use.use(Items.ENDER_PEARL, Client.getInstance().getModuleManager().get(KillAura.class).getTarget() != null, back.isEnabled());
            }
        }
    }
}
