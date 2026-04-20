package ru.etc1337.client.modules.impl.movement;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerAbilitiesPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventMove;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.packet.EventSendPacket;
import ru.etc1337.api.game.Inventory;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;

@ModuleInfo(name = "AirStuck", description = "Замораживает игрока", category = ModuleCategory.MOVEMENT)
public class Freeze extends Module {
    private final BooleanSetting auto = new BooleanSetting("Свапать элитру", this);
    public boolean isElytraState;

    @Compile
    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate eventUpdate) {
            isElytraState = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem().equals(Items.ELYTRA);
            if (isElytraState && auto.isEnabled()) {
                swap(true);
            }
        }

        if (event instanceof EventMove eventMove && !Client.getInstance().getModuleManager().get(ElytraJump.class).isEnabled()) {
            eventMove.getMotion().x = 0;
            eventMove.getMotion().y = 0;
            eventMove.getMotion().z = 0;
        }

        if (event instanceof EventSendPacket eventSendPacket && mc.player != null && !mc.player.isElytraFlying()) {
            IPacket<?> packet = eventSendPacket.getPacket();

            if (packet instanceof CPlayerPacket || packet instanceof CPlayerAbilitiesPacket) {
                event.setCancelled(true);
            }
        }
    }

    @Compile
    private void swap(boolean chestplate) {
        int chestPlate = chestplate ? Inventory.findChestPlate() : Inventory.findItem(Items.ELYTRA);

        if (chestPlate != -1) {
            Inventory.moveItem(chestPlate, 6);
        }
    }

    @Override
    public void onEnable() {
        isElytraState = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem().equals(Items.ELYTRA);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (isElytraState && auto.isEnabled()) {
            swap(false);
        }
        super.onDisable();
    }
}
