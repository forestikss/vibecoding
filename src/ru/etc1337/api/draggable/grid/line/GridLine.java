package ru.etc1337.api.draggable.grid.line;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.vector.Vector4f;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.render.Render;

@Getter
@RequiredArgsConstructor
public class GridLine implements QuickImports {
	private final float coordinate;
	private final GridRotationType rotationType;

	public void render() {
		MatrixStack matrixStack = new MatrixStack();
		Vector4f rect = getLineRect();
		Render.drawRect(matrixStack, rect.getX(), rect.getY(),
				rect.getZ(), rect.getW(), FixColor.WHITE);
	}

	public Vector4f getLineRect() {
		return rotationType == GridRotationType.HORIZONTAL
				? new Vector4f(0, coordinate, window.getScaledWidth(), 1)
				: new Vector4f(coordinate, 0, 1, window.getScaledHeight());
	}
}