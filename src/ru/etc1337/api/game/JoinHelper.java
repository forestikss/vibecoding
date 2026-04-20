package ru.etc1337.api.game;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.handler.EventListener;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.game.EventWorldChanged;
import ru.etc1337.api.events.impl.packet.EventReceivePacket;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.notifications.Notification;

@Getter @Setter
public class JoinHelper implements QuickImports, EventListener {
    public boolean enabled;
    public int grief;
    
    @Override
    public void onEvent(Event event) {
        if (!enabled) return;
        if (event instanceof EventUpdate) {
            checkGriefContainer();
        }
        if (event instanceof EventReceivePacket e) joinSuccessful(e);
        if (event instanceof EventWorldChanged) {
            enabled = false;
        }
    }
    public void disable() {
        this.enabled = false;
    };
    public void join(boolean state, int grief) {
        this.enabled = state;
        this.grief = grief;
        if (state) {
            if (Server.isRW()) {
                joinerItem();
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            }
        }
    }

    private void joinSuccessful(EventReceivePacket event) {
        if (Server.isRW()) {
            if (event.getPacket() instanceof SChatPacket sChatPacket) {
                String message = sChatPacket.getChatComponent().getString();
                if (message.contains("Подождите несколько секунд перед повторым подключением!")) {
                    event.setCancelled(true);
                } else if (message.contains("К сожалению сервер переполнен")) {
                    event.setCancelled(true);
                    Chat.send(String.format("Вход на гриф #%s: " + TextFormatting.RED + "Неудачно", (int) this.grief));
                }
            }
            if (event.getPacket() instanceof SJoinGamePacket) {
                Chat.send(String.format("Вход на гриф #%s: " + TextFormatting.GREEN + "Успешно", (int) this.grief));
                Client.getInstance().getNotificationManager().publicity("Вход на гриф #" +  (int) this.grief, "Успешно", 2,
                        Notification.Type.CUSTOM, Notification.SoundType.NOTIFY);
                enabled = false;
            } else if (event.getPacket() instanceof SChatPacket) {
                joinerItem();
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            } else if (event.getPacket() instanceof SOpenWindowPacket packet) {
                if (packet.getTitle().getString().contains("Выбор сервера")) {
                    mc.player.connection.sendPacket(new CClickWindowPacket(packet.getWindowId(),
                            21, 0, ClickType.PICKUP,
                            mc.player.openContainer.getSlot(21).getStack(),
                            mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
                    event.setCancelled(true);
                }
            }
        }
    }



    private void joinerItem() {
        int slot = Inventory.findItem(Items.COMPASS);
        if (slot == -1) return;
        mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
    }

    private void checkGriefContainer() {
        if (mc.player.openContainer == null) return;

        for (int slot = 0; slot < mc.player.openContainer.inventorySlots.size(); slot++) {
            Slot containerSlot = mc.player.openContainer.getSlot(slot);
            if (containerSlot != null && containerSlot.getHasStack()) {
                ItemStack stack = containerSlot.getStack();
                String itemName = stack.getDisplayName().getString();
                String string = String.format("ГРИФ #%s (1.16.5+)", (int)grief);
                if (itemName.equalsIgnoreCase(string)) {
                    mc.player.connection.sendPacket(new CClickWindowPacket(mc.player.openContainer.windowId,
                            slot, 0, ClickType.PICKUP,
                            stack,
                            mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
                }
            }
        }
    }
}
