package ru.etc1337.api.render.shaders.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.shaders.Shader;

import static org.lwjgl.opengl.GL11.*;

public class Outline extends Shader {
	
	private static final Outline shader = new Outline();
	
	public static void draw(MatrixStack matrixStack, Rect rect, float round, float width, FixColor color) {
		draw(matrixStack, rect, round, width, color, color, color, color);
	}
	
	public static void draw(MatrixStack matrixStack, Rect rect, float round, float width, FixColor color1, FixColor color2, FixColor color3, FixColor color4) {
	    Render.resetColor();

	    GlStateManager.enableBlend();
	    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	    Render.setAlphaLimit(0);

	    shader.start();

	    shader.setFloat("location", rect.getX() * window.getGuiScaleFactorFloat(),
	            (window.getScaledHeight() * window.getGuiScaleFactorFloat() - (rect.getHeight() * window.getGuiScaleFactorFloat())) - (rect.getY() * window.getGuiScaleFactorFloat()));
	    shader.setFloat("rectSize", rect.getWidth() * window.getGuiScaleFactorFloat(), rect.getHeight() * window.getGuiScaleFactorFloat());
	    shader.setFloat("radius", round * window.getGuiScaleFactorFloat());
	    shader.setFloat("outline", width * window.getGuiScaleFactorFloat());

	    shader.setFloat("color1", color1.getRed() / 255f, color1.getGreen() / 255f, color1.getBlue() / 255f, color1.getAlpha() / 255f);
	    shader.setFloat("color2", color2.getRed() / 255f, color2.getGreen() / 255f, color2.getBlue() / 255f, color2.getAlpha() / 255f);
	    shader.setFloat("color3", color3.getRed() / 255f, color3.getGreen() / 255f, color3.getBlue() / 255f, color3.getAlpha() / 255f);
	    shader.setFloat("color4", color4.getRed() / 255f, color4.getGreen() / 255f, color4.getBlue() / 255f, color4.getAlpha() / 255f);

	    rect = rect.size(-0.25f);
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
			uniform float radius, outline;
			
			float roundedSDF(vec2 centerPos, vec2 size, float radius) {
				return length(max(abs(centerPos) - size, 0.0)) - radius;
			}
			
			vec4 createGradient(vec2 pos) {
				return mix(mix(color1, color2, pos.y), mix(color3, color4, pos.y), pos.x);
			}
			
			void main() {
				vec2 texCoord = gl_TexCoord[0].st;
				vec2 centerPos = (rectSize * 0.5) - (texCoord * rectSize);
				vec2 size = (rectSize * 0.5) - radius - 1.0;
			
				float distance = roundedSDF(centerPos, size, radius);
				float blendAmount = abs(distance) - (outline * 0.5);
			
				vec4 gradient = createGradient(texCoord);
				vec4 insideColor = vec4(0.0, 0.0, 0.0, 0.0); // Пустой цвет внутри
			
				gl_FragColor = mix(gradient, insideColor, blendAmount);
			}


			""";
	}
}
