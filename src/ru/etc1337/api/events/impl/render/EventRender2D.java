package ru.etc1337.api.events.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.etc1337.api.events.impl.render.base.Base2D;

public class EventRender2D extends Base2D {

	public EventRender2D(MatrixStack matrixStack, float partialTicks) {
		super(matrixStack, partialTicks);
	}
	
}
