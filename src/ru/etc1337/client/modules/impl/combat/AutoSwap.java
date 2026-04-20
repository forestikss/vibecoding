package ru.etc1337.client.modules.impl.combat;

import com.ibm.icu.impl.Pair;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.input.EventInputKey;
import ru.etc1337.api.game.Inventory;
import ru.etc1337.api.settings.impl.BindSetting;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;

@ModuleInfo(name = "Auto Swap", description = "Свапает предмет по кнопке", category = ModuleCategory.COMBAT)
public class AutoSwap extends Module {
    private final BindSetting swapKey = new BindSetting("Кнопка свапа", this, -1);

    public final ModeSetting firstItem = new ModeSetting("Первый свап", this,
            "Руна","Тотем", "Шар", "Золотое яблоко", "Щит");

    public final ModeSetting secondItem = new ModeSetting("Второй свап", this,
            "Руна", "Тотем", "Шар", "Золотое яблоко", "Щит");

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventInputKey e) {
            if (e.isReleased()) return;
            if (swapKey.getKey() == e.getKey()) {
                handleEvent();
            }
        }
    }

    private Pair<Item, Item> getSwapPair() {
        return Pair.of(getItem(firstItem.getCurrentMode()), getItem(secondItem.getCurrentMode()));
    }
    @Compile
    private Item getItem(String itemName) {
        return switch (itemName) {
            case "Руна" -> Items.FIREWORK_STAR;
            case "Тотем" -> Items.TOTEM_OF_UNDYING;
            case "Шар" -> Items.PLAYER_HEAD;
            case "Золотое яблоко" -> Items.GOLDEN_APPLE;
            case "Щит" -> Items.SHIELD;
            default -> Items.AIR;
        };
    }

    private void handleEvent() {
        Item offhandItem = mc.player.getHeldItemOffhand().getItem();
        Slot first = Inventory.getSlot(getSwapPair().first);
        Slot second = Inventory.getSlot(getSwapPair().second);
        if (first == null || second == null) return;
        if (first.slotNumber != -1 || second.slotNumber != -1) {
            Slot validSlot = offhandItem != first.getStack().getItem() ? first : second;
            Inventory.swapHand(validSlot.slotNumber, Hand.OFF_HAND);
        }
    }
}
