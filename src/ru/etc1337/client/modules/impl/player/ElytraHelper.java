package ru.etc1337.client.modules.impl.player;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.text.TextFormatting;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.input.EventInputKey;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.game.Inventory;
import ru.etc1337.api.settings.impl.BindSetting;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Elytra Helper", description = "Помощник в использовании Элитр", category = ModuleCategory.PLAYER)
public class ElytraHelper extends Module {
    private final BindSetting swapBind = new BindSetting("Кнопка Свапа", this, -1);
    private final BindSetting fireworkBind = new BindSetting("Бинд Фейерверка", this, -1);
    private final BooleanSetting auto = new BooleanSetting("Авто Взлет", this);

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventInputKey eventInputKey && !eventInputKey.isReleased()) {
            if (eventInputKey.getKey() == swapBind.getKey()) swap();
            if (eventInputKey.getKey() == fireworkBind.getKey() && mc.player.isElytraFlying()) Inventory.Use.use(Items.FIREWORK_ROCKET, false, true);
        }
        if (event instanceof EventUpdate eventUpdate) {
            if (this.auto.isEnabled()) {
                if (mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA && !mc.player.isInLava() && !mc.player.isInWater() && mc.player.isOnGround() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.jump();
                } else if (ElytraItem.isUsable(Items.ELYTRA.getDefaultInstance()) && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA && !mc.player.isElytraFlying() && !mc.player.isOnGround()) {
                    mc.player.startFallFlying();
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                }
            }
        }
    }

    private void swap() {
        boolean elytraOnChest = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem().equals(Items.ELYTRA);
        int chestPlate = elytraOnChest ? Inventory.findChestPlate() : Inventory.findItem(Items.ELYTRA);

        if (chestPlate != -1) {
            Inventory.moveItem(chestPlate, 6);
            Chat.send("Свапнул на " + (elytraOnChest ? (TextFormatting.AQUA + "Нагрудник") : (TextFormatting.RED + "Элитры")));
        }
    }
}