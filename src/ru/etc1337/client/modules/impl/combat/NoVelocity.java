package ru.etc1337.client.modules.impl.combat;

import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.packet.EventReceivePacket;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "No Velocity", description = "Не отталкивает игрока", category = ModuleCategory.COMBAT)
public class NoVelocity extends Module {
    ModeSetting mode = new ModeSetting("Mode", this, "Пакетный", "Обычный");
    int toSkip;

    @Override
    public void onEvent(Event event) {
        if (mc.player == null || mc.world == null) return;
        if (event instanceof EventReceivePacket eventPacket) {
            if (mode.is("Пакетный")) {
                if (!(eventPacket.getPacket() instanceof SEntityVelocityPacket)) {
                    return;
                }
                eventPacket.setCancelled(true);
            }
            if (mode.is("Обычный")) {
                IPacket iPacket = eventPacket.getPacket();
                if (iPacket instanceof SEntityVelocityPacket sEntityVelocityPacket) {
                    if (sEntityVelocityPacket.getEntityID() != mc.player.getEntityId() || this.toSkip < 0) {
                        return;
                    }
                    this.toSkip = 8;
                    event.setCancelled(true);
                }
                if (eventPacket.getPacket() instanceof SConfirmTransactionPacket) {
                    if (this.toSkip < 0) {
                        ++this.toSkip;
                    } else if (this.toSkip > 1) {
                        --this.toSkip;
                        event.setCancelled(true);
                    }
                }
                if (!(eventPacket.getPacket() instanceof SPlayerPositionLookPacket)) {
                    return;
                }
                this.toSkip = -8;
            }
        }
    }

    private void reset() {
        this.toSkip = 0;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.reset();
    }
}
