package ru.etc1337.api.render.shaders.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.shaders.Shader;

import static org.lwjgl.opengl.GL11.*;

public class Msdf extends Shader {

    private static final Msdf shader = new Msdf();

    public static void draw(Runnable runnable, MatrixStack matrixStack, Rect rect,
                            float range, float edgeStrength, float thickness, FixColor color, boolean outline, float outlineThickness, FixColor outlineColor) {
        shader.start();

        shader.setInt("Sampler", 0);
        shader.setFloat("TextureSize", rect.getWidth(), rect.getHeight());
        shader.setFloat("Range", range);
        shader.setFloat("EdgeStrength", edgeStrength);
        shader.setFloat("Thickness", thickness);
        shader.setFloat("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        shader.setInt("Outline", outline ? 1 : 0);
        shader.setFloat("OutlineThickness", outlineThickness);
        shader.setFloat("OutlineColor", outlineColor.getRed() / 255f, outlineColor.getGreen() / 255f, outlineColor.getBlue() / 255f, outlineColor.getAlpha() / 255f);

        runnable.run();

        shader.finish();
    }

    @Override
    public String getCode() {
        return """
                #version 120
                
                uniform sampler2D Sampler;
                uniform vec2 TextureSize;
                uniform float Range; // distance field range of the msdf font texture
                uniform float EdgeStrength;
                uniform float Thickness;
                uniform vec4 color;
                uniform bool Outline; // if false, outline computation will be ignored (and its uniforms)
                uniform float OutlineThickness;
                uniform vec4 OutlineColor;
                
                float median(float red, float green, float blue) {
                    return max(min(red, green), min(max(red, green), blue));
                }
                
                void main() {
                    vec4 texColor = texture2D(Sampler, gl_TexCoord[0].st);
                
                    float dx = dFdx(gl_TexCoord[0].x) * TextureSize.x;
                    float dy = dFdy(gl_TexCoord[0].y) * TextureSize.y;
                    float toPixels = Range * inversesqrt(dx * dx + dy * dy);
                
                    float sigDist = median(texColor.r, texColor.g, texColor.b) - 0.5 + Thickness;
                
                    float alpha = smoothstep(-EdgeStrength, EdgeStrength, sigDist * toPixels);
                    if (Outline) {
                        float outlineAlpha = smoothstep(-EdgeStrength, EdgeStrength, (sigDist + OutlineThickness) * toPixels) - alpha;
                        float finalAlpha = alpha * color.a + outlineAlpha * color.a;
                        gl_FragColor = vec4(mix(OutlineColor.rgb, color.rgb, alpha), finalAlpha);
                    } else {
                        gl_FragColor = vec4(color.rgb, color.a * alpha);
                    }
                }
                """;
    }
}