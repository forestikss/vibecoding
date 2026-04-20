package ru.etc1337.api.render.shaders.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.shaders.Shader;

public class Glow extends Shader {
	
	private static final Glow shader = new Glow();

	public static void draw(MatrixStack matrixStack, Rect rect, float radius, float alpha, float round, FixColor color1, FixColor color2, FixColor color3, FixColor color4) {
		draw(matrixStack, rect.size(-radius), true, alpha, radius * window.getGuiScaleFactorFloat(), -radius, radius * window.getGuiScaleFactorFloat(), round, color1, color2, color3, color4);
	}
	
	public static void draw(MatrixStack matrixStack, Rect rect, boolean shadow, float shadowAlpha, float value, float smoothness1, float smoothness2, float round, FixColor color1, FixColor color2, FixColor color3, FixColor color4) {
	    Render.resetColor();
	    GlStateManager.enableBlend();
	    RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	    Render.setAlphaLimit(0);

	    shader.start();

	    shader.setFloat("size", (float) (rect.getWidth() * window.getGuiScaleFactor()), (float) (rect.getHeight() * window.getGuiScaleFactor()));
        shader.setFloat("round", round, round, round, round);
        shader.setFloat("smoothness", smoothness1, smoothness2);
        shader.setFloat("value", value);
        shader.setInt("shadow", shadow ? 1 : 0);
        shader.setFloat("shadowAlpha", shadow ? shadowAlpha : 0);
        shader.setFloat("color1", FixColor.getRGBAFloat(color1));
        shader.setFloat("color2", FixColor.getRGBAFloat(color2));
        shader.setFloat("color3", FixColor.getRGBAFloat(color3));
        shader.setFloat("color4", FixColor.getRGBAFloat(color4));

	    Shader.drawQuads(matrixStack, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	    shader.finish();

	    GlStateManager.disableBlend();
	}

	@Override
	public String getCode() {
		return """
				#version 120
                
                uniform vec4 color1;
                uniform vec4 color2;
                uniform vec4 color3;
                uniform vec4 color4;
                
                uniform vec2 size;
                uniform vec4 round;
                uniform float value;
                uniform vec2 smoothness;
                uniform bool shadow;
                uniform float shadowAlpha;
                
                float roundedBox(vec2 center, vec2 size, vec4 radius) {
                    radius.xy = (center.x > 0.0) ? radius.xy : radius.zw;
                    radius.x  = (center.y > 0.0) ? radius.x : radius.y;
                
                    vec2 q = abs(center) - size + radius.x;
                    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius.x;
                }
                
                vec4 createGradient(vec2 pos) {
                    return mix(mix(color1, color2, pos.y), mix(color3, color4, pos.y), pos.x);
                }
                
                void main() {
                    vec2 tex = gl_TexCoord[0].st * size;
                
                    float distance = roundedBox(tex - (size / 2.0), (size / 2.0) - value, round);
                
                    float smoothedAlpha = (1.0 - smoothstep(smoothness.x, smoothness.y, distance));
                
                    vec4 gradient = createGradient(gl_TexCoord[0].st);
                
                    if (shadow) {
                        vec4 finalColor = mix(vec4(gradient.rgb, 0.0), vec4(gradient.rgb, gradient.a * smoothedAlpha), smoothedAlpha);
                        gl_FragColor = vec4(finalColor.rgb, finalColor.a * shadowAlpha);
                    } else {
                        gl_FragColor = vec4(gradient.rgb, gradient.a * smoothedAlpha);                        
                    }
                }
				""";
	}
	
}
