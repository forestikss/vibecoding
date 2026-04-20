package ru.etc1337.api.render.shaders.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.shaders.Shader;

import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX;
import static org.lwjgl.opengl.GL11.*;

public class Round extends Shader {

    private static Round shader;
    private static Shader textured;
    private static Shader face;
    private static Shader outlined;

    // Флаг для определения AMD видеокарты
    public static boolean isAMDGPU = false;

    // Инициализация шейдеров с проверкой
    static {
        // Определяем тип видеокарты при инициализации
        detectGPUType();

        try {
            shader = new Round();
            if (!shader.isValid()) {
                System.err.println("Failed to create main Round shader");
                shader = null;
            }
        } catch (Exception e) {
            System.err.println("Exception creating Round shader: " + e.getMessage());
            shader = null;
        }

        try {
            textured = new Shader() {
                @Override
                public String getCode() {
                    return "#version 120\n" +
                            "\n" +
                            "uniform vec2 rectSize;\n" +
                            "uniform sampler2D textureIn;\n" +
                            "uniform float radius, alpha;\n" +
                            "uniform float scale = 1.0;\n" +
                            "uniform vec2 imagePos = vec2(0.0, 0.0);\n" +
                            "\n" +
                            "float roundedBoxSDF(vec2 centerPos, vec2 size, float radius) {\n" +
                            "    return length(max(abs(centerPos) - size, 0.0)) - radius;\n" +
                            "}\n" +
                            "\n" +
                            "void main() {\n" +
                            "    float distance = roundedBoxSDF((rectSize * 0.5) - (gl_TexCoord[0].st * rectSize), (rectSize * 0.5) - radius - 1.0, radius);\n" +
                            "    vec2 scaledTexCoord = (gl_TexCoord[0].st - 0.5) / scale + 0.5 + imagePos;\n" +
                            "    vec4 texColor = texture2D(textureIn, scaledTexCoord);\n" +
                            "    float smoothedAlpha = (1.0 - smoothstep(0.0, 2.0, distance)) * alpha;\n" +
                            "    gl_FragColor = vec4(texColor.rgb, smoothedAlpha);\n" +
                            "}\n";
                }
            };
            if (!textured.isValid()) {
                System.err.println("Failed to create textured shader");
                textured = null;
            }
        } catch (Exception e) {
            System.err.println("Exception creating textured shader: " + e.getMessage());
            textured = null;
        }

        try {
            face = new Shader() {
                @Override
                public String getCode() {
                    return  "#version 120\n" +
                            "\n" +
                            "uniform vec2 location, size;\n" +
                            "uniform sampler2D texture;\n" +
                            "uniform float radius, alpha;\n" +
                            "uniform float u, v, w, h;\n" +
                            "\n" +
                            "float calcLength(vec2 p, vec2 b, float r) {\n" +
                            "    return length(max(abs(p) - b, 0.0)) - r;\n" +
                            "}\n" +
                            "\n" +
                            "void main() {\n" +
                            "    vec2 halfSize = size * 0.5;\n" +
                            "    vec2 st = gl_TexCoord[0].st;\n" +
                            "    st.x = u + st.x * w;\n" +
                            "    st.y = v + st.y * h;\n" +
                            "    float distance = calcLength(halfSize - (gl_TexCoord[0].st * size), halfSize - radius - 1.0, radius);\n" +
                            "    float smoothedAlpha = (1.0 - smoothstep(0.0, 2.0, distance)) * alpha;\n" +
                            "    vec4 color = texture2D(texture, st);\n" +
                            "    gl_FragColor = vec4(color.rgb, smoothedAlpha);\n" +
                            "}\n";
                }
            };
            if (!face.isValid()) {
                System.err.println("Failed to create face shader");
                face = null;
            }
        } catch (Exception e) {
            System.err.println("Exception creating face shader: " + e.getMessage());
            face = null;
        }

        try {
            outlined = new Shader() {
                @Override
                public String getCode() {
                    return "#version 120\n" +
                            "\n" +
                            "uniform vec2 rectSize;\n" +
                            "uniform vec4 color, outlineColor;\n" +
                            "uniform float radius, outline;\n" +
                            "\n" +
                            "float roundedSDF(vec2 centerPos, vec2 size, float radius) {\n" +
                            "    return length(max(abs(centerPos) - size, 0.0)) - radius;\n" +
                            "}\n" +
                            "\n" +
                            "void main() {\n" +
                            "    float distance = roundedSDF((rectSize * 0.5) - (gl_TexCoord[0].st * rectSize), (rectSize * 0.5) - radius - 1.0, radius);\n" +
                            "    float blendAmount = smoothstep(-0.5, 0.5, abs(distance) - (outline * 0.5));\n" +
                            "    vec4 insideColor = (distance < 0.0) ? color : vec4(outlineColor.rgb, 0.0);\n" +
                            "    gl_FragColor = mix(outlineColor, insideColor, blendAmount);\n" +
                            "}\n";
                }
            };
            if (!outlined.isValid()) {
                System.err.println("Failed to create outlined shader");
                outlined = null;
            }
        } catch (Exception e) {
            System.err.println("Exception creating outlined shader: " + e.getMessage());
            outlined = null;
        }

        System.out.println("GPU Detection: AMD=" + isAMDGPU + ", Vendor: " + getGPUVendor());
    }

    // Метод для определения типа видеокарты
    // Метод для определения типа видеокарты
    private static void detectGPUType() {
        try {
            String vendor = glGetString(GL_VENDOR);
            String renderer = glGetString(GL_RENDERER);

            if (vendor != null) {
                String vendorLower = vendor.toLowerCase();
                String rendererLower = renderer != null ? renderer.toLowerCase() : "";

                // Правильная логика определения AMD
                isAMDGPU = vendorLower.contains("amd") ||
                        vendorLower.contains("advanced micro devices") ||
                        vendorLower.contains("ati technologies") ||
                        rendererLower.contains("radeon") ||
                        rendererLower.contains("amd");

                System.out.println("GPU Vendor: " + vendor);
                System.out.println("GPU Renderer: " + renderer);
                System.out.println("Detected as AMD: " + isAMDGPU);
            }
        } catch (Exception e) {
            System.err.println("Failed to detect GPU type: " + e.getMessage());
            isAMDGPU = false; // По умолчанию false при ошибке
        }
    }

    // Метод для получения информации о вендоре видеокарты
    private static String getGPUVendor() {
        try {
            return glGetString(GL_VENDOR) + " / " + glGetString(GL_RENDERER);
        } catch (Exception e) {
            return "Unknown";
        }
    }

    // Метод для принудительной установки режима AMD (для тестирования)
    public static void setAMDGPUMode(boolean amdMode) {
        isAMDGPU = amdMode;
        System.out.println("AMD GPU mode " + (amdMode ? "enabled" : "disabled"));
    }

    public static void draw(MatrixStack matrixStack, Rect rect, float round, FixColor color) {
        draw(matrixStack, rect, round, color, color, color, color);
    }

    public static float calc(float value) {
        return (float) (value * window.getGuiScaleFactor() / 2);
    }

    public static void drawRoundCircle(MatrixStack stack, float x, float y, float radius, FixColor color) {
        Round.draw(stack, new Rect(x - (radius / 2), y - (radius / 2), radius, radius), radius, color);
    }

    public static void draw(MatrixStack matrixStack, Rect rect, float topLeft, float topRight, float bottomLeft, float bottomRight, FixColor color) {
        draw(matrixStack, rect, topLeft, topRight, bottomLeft, bottomRight, color, color, color, color);
    }

    public static void draw(MatrixStack matrixStack, Rect rect, float round, FixColor gradientColor1, FixColor gradientColor2, FixColor gradientColor3, FixColor gradientColor4) {
        draw(matrixStack, rect, round, round, round, round, gradientColor1, gradientColor2, gradientColor3, gradientColor4);
    }

    public static void draw(MatrixStack matrixStack, Rect rect, float topLeft, float topRight, float bottomLeft, float bottomRight, FixColor gradientColor1, FixColor gradientColor2, FixColor gradientColor3, FixColor gradientColor4) {
        if (shader == null || !shader.isValid()) {
            System.err.println("Round shader is not available, skipping render");
            return;
        }

        if (topLeft == 0) topLeft = 0.001F;
        if (topRight == 0) topRight = 0.001F;
        if (bottomLeft == 0) bottomLeft = 0.001F;
        if (bottomRight == 0) bottomRight = 0.001F;

        if (!Shader.FLAT_RENDERER) {
            GlStateManager.enableBlend();
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            Render.setAlphaLimit(0);
        }

        try {
            shader.start();
            shader.setFloat("size", (float) (rect.getWidth() * window.getGuiScaleFactor()), (float) (rect.getHeight() * window.getGuiScaleFactor()));
            shader.setFloat("gradientColor1", gradientColor1.getRed() / 255F, gradientColor1.getGreen() / 255F, gradientColor1.getBlue() / 255F, gradientColor1.getAlpha() / 255F);
            shader.setFloat("gradientColor2", gradientColor2.getRed() / 255F, gradientColor2.getGreen() / 255F, gradientColor2.getBlue() / 255F, gradientColor2.getAlpha() / 255F);
            shader.setFloat("gradientColor3", gradientColor3.getRed() / 255F, gradientColor3.getGreen() / 255F, gradientColor3.getBlue() / 255F, gradientColor3.getAlpha() / 255F);
            shader.setFloat("gradientColor4", gradientColor4.getRed() / 255F, gradientColor4.getGreen() / 255F, gradientColor4.getBlue() / 255F, gradientColor4.getAlpha() / 255F);
            shader.setFloat("cornerRadius", (float) (topLeft * window.getGuiScaleFactor()), (float) (bottomLeft * window.getGuiScaleFactor()), (float) (topRight * window.getGuiScaleFactor()), (float) (bottomRight * window.getGuiScaleFactor()));
            drawQuads(matrixStack, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            shader.finish();
        } catch (Exception e) {
            System.err.println("Error drawing round rect: " + e.getMessage());
        }

        if (!Shader.FLAT_RENDERER) {
            GlStateManager.disableBlend();
        }
    }

    public static void drawFace(Rect rect, float radius, float alpha, AbstractClientPlayerEntity target) {
        if (face == null || !face.isValid()) {
            System.err.println("Face shader is not available, skipping render");
            return;
        }

        try {
            ResourceLocation skin = target.getLocationSkin();
            mc.getTextureManager().bindTexture(skin);
            RenderSystem.enableBlend();

            face.start();
            face.setFloat("location", rect.getX() * 2, Minecraft.getInstance().getMainWindow().getHeight() - rect.getHeight() * 2 - rect.getY() * 2);
            face.setFloat("size", rect.getWidth() * 2, rect.getHeight() * 2);
            face.setFloat("radius", radius * 2);
            face.setFloat("alpha", alpha);
            face.setFloat("u", (1f / 64) * 8);
            face.setFloat("v", (1f / 64) * 8);
            face.setFloat("w", 1f / 8);
            face.setFloat("h", 1f / 8);
            quadsBegin(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), 7);
            face.finish();

            RenderSystem.disableBlend();
        } catch (Exception e) {
            System.err.println("Error drawing face: " + e.getMessage());
        }
    }

    public static void drawTextured(MatrixStack matrixStack, Rect rect, float round, float alpha) {
        if (textured == null || !textured.isValid()) {
            System.err.println("Textured shader is not available, skipping render");
            return;
        }

        try {
            GlStateManager.enableBlend();
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            Render.setAlphaLimit(0);

            textured.start();
            textured.setFloat("rectSize", (float) (rect.getWidth() * window.getGuiScaleFactor()), (float) (rect.getHeight() * window.getGuiScaleFactor()));
            textured.setInt("textureIn", 0);
            textured.setFloat("alpha", alpha);
            textured.setFloat("radius", (float) (round * window.getGuiScaleFactor()));
            drawQuads(matrixStack, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            textured.finish();

            GlStateManager.disableBlend();
        } catch (Exception e) {
            System.err.println("Error drawing textured rect: " + e.getMessage());
        }
    }

    public static void drawOutlined(MatrixStack matrixStack, Rect rect, float round, float outline, FixColor color, FixColor outlineColor) {
        drawOutlined(matrixStack, rect, round, outline, color, outlineColor, false);
    }

    public static void drawOutlined(MatrixStack matrixStack, Rect rect, float round, float outline, FixColor color, FixColor outlineColor, boolean inner) {
        if (outlined == null || !outlined.isValid()) {
            System.err.println("Outlined shader is not available, skipping render");
            return;
        }

        try {
            GlStateManager.enableBlend();
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            Render.setAlphaLimit(0);

            outlined.start();
            outlined.setFloat("rectSize", (float) (rect.getWidth() * window.getGuiScaleFactor()), (float) (rect.getHeight() * window.getGuiScaleFactor()));
            outlined.setFloat("color", color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
            outlined.setFloat("outlineColor", outlineColor.getRed() / 255F, outlineColor.getGreen() / 255F, outlineColor.getBlue() / 255F, outlineColor.getAlpha() / 255F);
            outlined.setFloat("radius", (float) (round * window.getGuiScaleFactor()));
            outlined.setFloat("outline", outline);

            if (inner) {
                drawQuads(matrixStack, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            } else {
                drawQuads(matrixStack, rect.getX() - outline, rect.getY() - outline, rect.getWidth() + outline * 2, rect.getHeight() + outline * 2);
            }
            outlined.finish();

            GlStateManager.disableBlend();
        } catch (Exception e) {
            System.err.println("Error drawing outlined rect: " + e.getMessage());
        }
    }

    public static void quadsBegin(float x, float y, float width, float height, int glQuads) {
        try {
            tessellator.getBuffer().begin(glQuads, POSITION_TEX);
            tessellator.getBuffer().pos(x, y, 0).tex(0, 0).endVertex();
            tessellator.getBuffer().pos(x, y + height, 0).tex(0, 1).endVertex();
            tessellator.getBuffer().pos(x + width, y + height, 0).tex(1, 1).endVertex();
            tessellator.getBuffer().pos(x + width, y, 0).tex(1, 0).endVertex();
            tessellator.draw();
        } catch (Exception e) {
            System.err.println("Error in quadsBegin: " + e.getMessage());
        }
    }

    @Override
    public String getCode() {
        if (isAMDGPU) {
            // Оптимизированный шейдер для AMD видеокарт
            return "#version 120\n" +
                    "\n" +
                    "uniform vec2 size;\n" +
                    "uniform vec4 cornerRadius; // (topLeft, bottomLeft, topRight, bottomRight)\n" +
                    "uniform vec4 gradientColor1; // Top-Left color\n" +
                    "uniform vec4 gradientColor2; // Top-Right color\n" +
                    "uniform vec4 gradientColor3; // Bottom-Left color\n" +
                    "uniform vec4 gradientColor4; // Bottom-Right color\n" +
                    "\n" +
                    "// Signed Distance Function for a box with different radii for each corner\n" +
                    "float roundedBoxSDF(vec2 centerPos, vec2 boxSize, vec4 radii) {\n" +
                    "    float radius;\n" +
                    "    // Select correct radius based on the fragment's quadrant\n" +
                    "    // Y coordinate is negative in the top half, positive in the bottom half\n" +
                    "    if (centerPos.x < 0.0) { // Left half\n" +
                    "        if (centerPos.y < 0.0) { // Top-Left\n" +
                    "            radius = radii.x;\n" +
                    "        } else { // Bottom-Left\n" +
                    "            radius = radii.y;\n" +
                    "        }\n" +
                    "    } else { // Right half\n" +
                    "        if (centerPos.y < 0.0) { // Top-Right\n" +
                    "            radius = radii.z;\n" +
                    "        } else { // Bottom-Right\n" +
                    "            radius = radii.w;\n" +
                    "        }\n" +
                    "    }\n" +
                    "    \n" +
                    "    vec2 q = abs(centerPos) - boxSize + radius;\n" +
                    "    return length(max(q, 0.0)) - radius;\n" +
                    "}\n" +
                    "\n" +
                    "void main() {\n" +
                    "    // Calculate position relative to the center of the rectangle, from (-size/2) to (+size/2)\n" +
                    "    vec2 centerPos = (gl_TexCoord[0].st - 0.5) * size;\n" +
                    "    vec2 halfSize = 0.5 * size;\n" +
                    "\n" +
                    "    // Calculate signed distance from the edge of the rounded box\n" +
                    "    float distance = roundedBoxSDF(centerPos, halfSize, cornerRadius);\n" +
                    "\n" +
                    "    // Use smoothstep for anti-aliasing to get a smooth edge\n" +
                    "    float smoothedAlpha = 1.0 - smoothstep(0.0, 1.5, distance);\n" +
                    "\n" +
                    "    // Bi-linear interpolation for gradient colors\n" +
                    "    vec4 topMix = mix(gradientColor1, gradientColor2, gl_TexCoord[0].s);\n" +
                    "    vec4 bottomMix = mix(gradientColor3, gradientColor4, gl_TexCoord[0].s);\n" +
                    "    vec4 gradientColor = mix(topMix, bottomMix, gl_TexCoord[0].t);\n" +
                    "\n" +
                    "    // Set the final color, combining the gradient with the calculated alpha\n" +
                    "    gl_FragColor = vec4(gradientColor.rgb, gradientColor.a * smoothedAlpha);\n" +
                    "}\n";
        } else {
            // Стандартный шейдер для других видеокарт
            return "#version 120\n" +
                    "\n" +
                    "uniform vec4 cornerRadius;\n" +
                    "uniform vec2 size;\n" +
                    "uniform vec4 gradientColor1;\n" +
                    "uniform vec4 gradientColor2;\n" +
                    "uniform vec4 gradientColor3;\n" +
                    "uniform vec4 gradientColor4;\n" +
                    "\n" +
                    "float alpha(vec2 d, vec2 d1, vec4 radii) {\n" +
                    "    vec2 v = abs(d) - d1 + radii.xy;\n" +
                    "    v.x = max(v.x, 0.0);\n" +
                    "    v.y = max(v.y, 0.0);\n" +
                    "    return min(v.x, v.y) + length(max(v, vec2(0.0))) - radii.x;\n" +
                    "}\n" +
                    "\n" +
                    "void main() {\n" +
                    "    vec4 radii;\n" +
                    "    if (gl_TexCoord[0].s < 0.5) {\n" +
                    "        if (gl_TexCoord[0].t < 0.5) {\n" +
                    "            radii = vec4(cornerRadius.x, cornerRadius.x, cornerRadius.x, cornerRadius.x);\n" +
                    "        } else {\n" +
                    "            radii = vec4(cornerRadius.y, cornerRadius.y, cornerRadius.y, cornerRadius.y);\n" +
                    "        }\n" +
                    "    } else {\n" +
                    "        if (gl_TexCoord[0].t < 0.5) {\n" +
                    "            radii = vec4(cornerRadius.z, cornerRadius.z, cornerRadius.z, cornerRadius.z);\n" +
                    "        } else {\n" +
                    "            radii = vec4(cornerRadius.w, cornerRadius.w, cornerRadius.w, cornerRadius.w);\n" +
                    "        }\n" +
                    "    }\n" +
                    "\n" +
                    "    if (radii.x == 0.0 && radii.y == 0.0) {\n" +
                    "        gl_FragColor = vec4(gradientColor1.rgb, gradientColor1.a * 1.0);\n" +
                    "    } else {\n" +
                    "        vec2 delta = (abs(gl_TexCoord[0].st - 0.5) + 0.5) * size - size + radii.xy + 0.5;\n" +
                    "        float smooth = smoothstep(1.0, 0.0, length(max(delta, vec2(0.0))) - radii.x);\n" +
                    "\n" +
                    "        vec4 gradientColor = mix(mix(gradientColor1, gradientColor2, gl_TexCoord[0].s),\n" +
                    "                                 mix(gradientColor3, gradientColor4, gl_TexCoord[0].s),\n" +
                    "                                 gl_TexCoord[0].t);\n" +
                    "        gl_FragColor = vec4(gradientColor.rgb, gradientColor.a * clamp(smooth, 0.0, 1.0));\n" +
                    "    }\n" +
                    "}\n";
        }
    }
}