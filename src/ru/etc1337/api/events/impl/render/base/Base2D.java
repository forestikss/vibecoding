package ru.etc1337.api.events.impl.render.base;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.etc1337.api.events.Event;

@Getter
@AllArgsConstructor
public abstract class Base2D extends Event {

    private final MatrixStack matrixStack;
    private final float partialTicks;

}
