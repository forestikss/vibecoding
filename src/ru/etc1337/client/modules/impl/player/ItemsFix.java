package ru.etc1337.client.modules.impl.player;

import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.ShulkerBoxScreen;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventSlotClick;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.packet.EventReceivePacket;
import ru.etc1337.api.events.impl.packet.EventSendPacket;
import ru.etc1337.api.game.Inventory;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Items Fix", description = "Фиксит перемещения предметов", category = ModuleCategory.PLAYER)
public class ItemsFix extends Module {
    private final MultiModeSetting components = new MultiModeSetting("Компоненты", this, "Не свапать слоты", "Не выкидывать элитру", "Не выкидывать нагрудник", "Не выкидывать шар");

    @Override
    public void onEvent(Event event) {
        if (mc.player == null || mc.world == null) return;
        if (event instanceof EventUpdate) {
            this.aHc();
        }
        if (event instanceof EventSlotClick eventSlotClick) {
            Slot zo2 = eventSlotClick.aDf();
            ClickType clickType = eventSlotClick.Wp();
            if (zo2 == null || clickType == ClickType.SWAP || clickType == ClickType.QUICK_MOVE) {
                return;
            }
            Item item = zo2.getStack().getItem();
            if (this.components.get(1).isEnabled() && item == Items.ELYTRA) {
                cancelEvent(eventSlotClick);
            }
            if (this.components.get(2).isEnabled() && item instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == EquipmentSlotType.CHEST) {
                cancelEvent(eventSlotClick);
            }
            if (this.components.get(3).isEnabled() && item == Items.PLAYER_HEAD) {
                cancelEvent(eventSlotClick);
            }
        }
        if (components.get(0).isEnabled()) {
            if (event instanceof EventReceivePacket eventReceivePacket) {
                if (eventReceivePacket.getPacket() instanceof SHeldItemChangePacket) {
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                    event.setCancelled(true);
                }
            }
        }
    }

    private void aHc() {
        if (mc.currentScreen instanceof ChestScreen || mc.currentScreen instanceof CreativeScreen || mc.currentScreen instanceof ShulkerBoxScreen) {
            return;
        }
        ItemStack itemStack = mc.player.inventory.getItemStack();
        int n = Inventory.getAirSlot();
        boolean bl = Inventory.hasAirSlotInInventory();
        if (this.components.get(1).isEnabled() && Inventory.findItem(Items.ELYTRA) == -1 && bl && itemStack.getItem() == Items.ELYTRA) {
            Inventory.doWindowClick(() -> mc.playerController.windowClick(0, n, 1, ClickType.PICKUP, mc.player));
        }
        if (this.components.get(2).isEnabled() && Inventory.findArmorSlot() == -1 && bl && itemStack.getItem() instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == EquipmentSlotType.CHEST) {
            Inventory.doWindowClick(() -> mc.playerController.windowClick(0, n, 1, ClickType.PICKUP, mc.player));
        }
        if (this.components.get(3).isEnabled() && Inventory.findItem(Items.PLAYER_HEAD) == -1 && bl && itemStack.getItem() == Items.PLAYER_HEAD) {
            Inventory.doWindowClick(() -> mc.playerController.windowClick(0, n, 1, ClickType.PICKUP, mc.player));
        }
    }

    private static void cancelEvent(EventSlotClick eventSlotClick) {
        eventSlotClick.setCancelled(true);
    }
}
