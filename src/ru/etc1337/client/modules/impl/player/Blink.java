package ru.etc1337.client.modules.impl.player;

import net.minecraft.client.settings.PointOfView;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.packet.EventSendPacket;
import ru.etc1337.api.events.impl.render.EventRenderWorldEntities;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

import java.util.ArrayList;

@ModuleInfo(name = "Blink", description = "Замораживает вас для сервера", category = ModuleCategory.PLAYER)
public class Blink extends Module {
	private final ArrayList<IPacket<?>> packets = new ArrayList<>();

	private final BooleanSetting visual = new BooleanSetting("Визуал", this);

	private Vector3d lastPos;
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventSendPacket e) {

			packets.add(e.getPacket());
			event.setCancelled(true);

		}
		
		if (event instanceof EventRenderWorldEntities e && visual.isEnabled() && lastPos != null) {
			Render.drawEntity3D(e.getMatrix(), mc.player, lastPos, 0.1f);
		}
	}
	
	@Override
	public void onDisable() {
		if (!mc.isSingleplayer()) {
			for (IPacket<?> p : packets) {
				mc.player.connection.sendPacketSilent(p);
			}
		}
		packets.clear();
		lastPos = null;
		super.onDisable();
	}

	@Override
	public void onEnable() {
		lastPos = mc.player.getPositionVec();
		super.onEnable();
	}
}
