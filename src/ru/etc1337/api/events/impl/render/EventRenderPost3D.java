package ru.etc1337.api.events.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.ActiveRenderInfo;
import ru.etc1337.api.events.Event;

@Getter
@AllArgsConstructor
public class EventRenderPost3D extends Event {

	private final MatrixStack matrixStack;
	
	private final float partialTicks;
	

}
