package ru.etc1337.client.modules.impl.misc;

import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.game.EventWorldChanged;
import ru.etc1337.api.events.impl.packet.EventReceivePacket;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.game.Inventory;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.game.Server;
import ru.etc1337.api.notifications.Notification;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Auto Join", description = "Автоматически заходит на сервер", category = ModuleCategory.MISC)
public class AutoJoin extends Module {
    public final SliderSetting grief = new SliderSetting("Grief", this, 5, 1, 54, 1);

    @Override
    public void onEnable() {
        Client.getInstance().getJoinHelper().join(true, (int) grief.getValue());
        super.onEnable();
    }

    @Override
    public void onDisable() {
        Client.getInstance().getJoinHelper().disable();
        super.onDisable();
    }


    /*   @Override
    public void onEnable() {
        if (Server.isRW()) {
            joinerItem();
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
        }
        super.onEnable();
    }*/
}
