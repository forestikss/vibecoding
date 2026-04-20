package ru.etc1337.api.render.shaders.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.shaders.Shader;

import static org.lwjgl.opengl.GL11.*;

public class CircularProgressBar extends Shader {
	
	private static final CircularProgressBar shader = new CircularProgressBar();
	
	public static void draw(MatrixStack matrixStack, Rect rect, float progress, float width, FixColor color) {
		draw(matrixStack, rect, progress, width, color, color, color, color);
	}
	
	public static void draw(MatrixStack matrixStack, Rect rect, float progress, float width,
			FixColor color1, FixColor color2, FixColor color3, FixColor color4) {
		draw(matrixStack, rect, progress, width, color1, color2, color3, color4, -90);
	}
	
	public static void draw(MatrixStack matrixStack, Rect rect, float progress, float width,
			FixColor color1, FixColor color2, FixColor color3, FixColor color4, float startAngle) {
	    
	    progress = Math.max(0.0f, Math.min(360.0f, progress));
	    
	    Render.resetColor();

	    GlStateManager.enableBlend();
	    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	    Render.setAlphaLimit(0);

	    shader.start();

	    shader.setFloat("location", rect.getX() * window.getGuiScaleFactorFloat(),
	            (window.getScaledHeight() * window.getGuiScaleFactorFloat() - (rect.getHeight() * window.getGuiScaleFactorFloat())) - (rect.getY() * window.getGuiScaleFactorFloat()));
	    shader.setFloat("rectSize", rect.getWidth() * window.getGuiScaleFactorFloat(), rect.getHeight() * window.getGuiScaleFactorFloat());
	    
	    shader.setFloat("progress", progress / 360.0f);
	    shader.setFloat("outline", width * window.getGuiScaleFactorFloat());
	    shader.setFloat("startAngle", startAngle);

	    shader.setFloat("color1", color1.getRed() / 255f, color1.getGreen() / 255f, color1.getBlue() / 255f, color1.getAlpha() / 255f);
	    shader.setFloat("color2", color2.getRed() / 255f, color2.getGreen() / 255f, color2.getBlue() / 255f, color2.getAlpha() / 255f);
	    shader.setFloat("color3", color3.getRed() / 255f, color3.getGreen() / 255f, color3.getBlue() / 255f, color3.getAlpha() / 255f);
	    shader.setFloat("color4", color4.getRed() / 255f, color4.getGreen() / 255f, color4.getBlue() / 255f, color4.getAlpha() / 255f);

	    drawQuads(matrixStack, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());

	    shader.finish();

	    GlStateManager.disableBlend();
	}

	@Override
	public String getCode() {
		return """
			#version 120
			
			uniform vec2 rectSize;
			uniform vec4 color1;
			uniform vec4 color2;
			uniform vec4 color3;
			uniform vec4 color4;
			uniform float progress;
			uniform float outline;
			uniform float startAngle;
			
			#define PI 3.14159265359
			
			vec4 createGradient(vec2 pos) {
				return mix(mix(color1, color2, pos.y), mix(color3, color4, pos.y), pos.x);
			}
			
			void main() {
				vec2 texCoord = gl_TexCoord[0].st;
				vec2 centerPos = texCoord - 0.5;
				
				// Расстояние от центра
				float distance = length(centerPos);
				
				// Радиус внешнего и внутреннего кругов
				float outerRadius = 0.5;
				float innerRadius = outerRadius - (outline / min(rectSize.x, rectSize.y));
				
				// Угол текущего пикселя
				float angle = atan(centerPos.y, centerPos.x);
				
				// Конвертируем угол в градусы и нормализуем (0-360)
				float angleDegrees = degrees(angle);
				if (angleDegrees < 0.0) angleDegrees += 360.0;
				
				// Применяем начальный угол смещения
				angleDegrees = mod(angleDegrees - startAngle + 360.0, 360.0);
				
				// Проверяем, находится ли пиксель в пределах прогресса
				float progressAngle = progress * 360.0;
				float isInProgress = step(angleDegrees, progressAngle);
				
				// Проверяем, находится ли пиксель в кольце
				float isInRing = step(innerRadius, distance) * step(distance, outerRadius);
				
				// Сглаживание краев
				float smoothInner = smoothstep(innerRadius - 0.01, innerRadius + 0.01, distance);
				float smoothOuter = 1.0 - smoothstep(outerRadius - 0.01, outerRadius + 0.01, distance);
				float smoothRing = smoothInner * smoothOuter;
				
				// Градиентный цвет
				vec4 gradient = createGradient(texCoord);
				
				// Финальный альфа канал
				float alpha = smoothRing * isInProgress;
				
				gl_FragColor = vec4(gradient.rgb, gradient.a * alpha);
			}
			""";
	}
}