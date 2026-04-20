package ru.etc1337.client.modules.impl.movement;

import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventNoSlow;
import ru.etc1337.api.events.impl.game.EventTick;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.game.Inventory;
import ru.etc1337.api.game.Move;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

@ModuleInfo(name = "No Slow", description = "Убирает замедление поедания", category = ModuleCategory.MOVEMENT)
public class NoSlow extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", this, "Grim Auto", "Grim Usable", "Grim Ticks");
    public final BooleanSetting sprintWhileUsing = new BooleanSetting("Sprint while using", this);
    private int ticks = 0;

    @Compile
    @Override
    public void onEvent(Event event) {
        if (mc.player == null || mc.world == null || mc.player.isElytraFlying()) return;
        if (mode.is("Grim Usable") || mode.is("Grim Auto") || mode.is("Grim Ticks")) {
            boolean haveCrossbow = Inventory.findItem(Items.CROSSBOW) != -1;
            boolean canWork = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.OFF_HAND && (haveCrossbow || Inventory.findItem(Items.BOW) != -1);
            if (mode.is("Grim Auto")) {
                if (!canWork) {
                    grimTicks(event);
                    return;
                }
                grimUsable(event, haveCrossbow);
            } else {
                if (mode.is("Grim Usable")) {
                    if (canWork) grimUsable(event, haveCrossbow);
                } else if (mode.is("Grim Ticks")) {
                    grimTicks(event);
                }
            }
        }
    }

    @Compile
    public void grimTicks(Event event) {
        if (event instanceof EventUpdate) {
            if (mc.player.isHandActive()) {
                ticks++;
            } else {
                ticks = 0;
            }
        }
        if (event instanceof EventNoSlow eventNoSlow) {
            if (ticks > 1F) {
                eventNoSlow.setCancelled(true);
                ticks = 0;
            }
        }
    }

    @Compile
    @VMProtect(type = VMProtectType.MUTATION)
    public void grimUsable(Event event, boolean haveCrossbow) {
        if (event instanceof EventUpdate && mc.player.ticksExisted % 2 == 0) {
            Inventory.Use.use(haveCrossbow ? Items.CROSSBOW : Items.BOW, false, false);
        }
        if (event instanceof EventNoSlow && mc.player.getItemInUseMaxCount() >= 2) {
            event.setCancelled(true);
        }
    }
}