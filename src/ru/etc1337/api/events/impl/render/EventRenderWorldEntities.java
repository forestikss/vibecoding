package ru.etc1337.api.events.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.vector.Matrix4f;
import ru.etc1337.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
public final class EventRenderWorldEntities extends Event {
    private MatrixStack matrix;
    private Matrix4f projectionMatrix;
    private ActiveRenderInfo activeRenderInfo;
    private WorldRenderer context;
    private float partialTicks;
    private long finishTimeNano;
    private double x, y, z;
    private IRenderTypeBuffer vertex;
}