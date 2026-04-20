package ru.etc1337.api.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.*;
import net.optifine.Config;
import net.optifine.shaders.Shaders;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.game.Maths;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.fonts.impl.CFontRenderer;
import ru.etc1337.api.render.shaders.Shader;
import ru.etc1337.api.render.shaders.impl.Glow;
import ru.etc1337.api.render.shaders.impl.Outline;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.client.modules.impl.render.AspectRatio;
import ru.etc1337.client.modules.impl.render.NoRender;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

import static net.minecraft.client.renderer.WorldRenderer.frustum;
import static org.lwjgl.opengl.GL11.GL_QUADS;

@UtilityClass
public class Render implements QuickImports {
    private static final Shader substringShader = new Shader() {
        @Override
        public String getCode() {
            return  """
            #version 120

            uniform sampler2D font;
            uniform vec4 inColor;
            uniform float width;
            uniform float maxWidth;

            void main() {
                float f = clamp(smoothstep(0.5, 1, 1 - (gl_FragCoord.x - maxWidth) / width), 0, 1);
                vec2 pos = gl_TexCoord[0].xy;
                vec4 color = texture2D(font, pos);

                if (color.a > 0) color.a = color.a * f;

                gl_FragColor = color * inColor;
            }
            """;
        }
    };
    public static void scaleStart(double x, double y, double scale) {
        RenderSystem.pushMatrix();
        RenderSystem.translated(x, y, 0);
        RenderSystem.scaled(scale, scale, 1);
        RenderSystem.translated(- x, - y, 0);
    }
    public static void scaleEnd() {
        RenderSystem.popMatrix();
    }

    public void setupOrientationMatrix(MatrixStack matrix, double x, double y, double z) {
        matrix.translate(x - cameraPos().x, y - cameraPos().y, z - cameraPos().z);
    }

    public void renderItem(ItemStack itemStack, int x, int y) {
        mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, x, y);
        mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, x, y, null);
    }

    public static void drawCircle(MatrixStack matrix, float x, float y, float radius, FixColor color) {
        Round.draw(matrix, new Rect(x - radius / 2f, y - radius / 2f, radius, radius), radius / 2f, color);
    }

    public void start() {
        RenderSystem.clearCurrentColor();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableDepthTest();
        RenderSystem.shadeModel(7425);
        defaultAlphaFunc();
    }

    public void stop() {
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.clearCurrentColor();
        RenderSystem.shadeModel(7424);
    }

    public void defaultAlphaFunc() {
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0);
    }

    public void startImageRendering(ResourceLocation resourceLocation) {
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0);

        mc.getTextureManager().bindTexture(resourceLocation);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void finishImageRendering() {
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    public void drawImage(MatrixStack matrices, ResourceLocation location, double x, double y, double width, double height, Color color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        mc.getTextureManager().bindTexture(location);
        builder.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
        builder.pos(matrices, x, y, 0).color(color.getRGB()).tex(0, 0).endVertex();
        builder.pos(matrices, x, y + height, 0).color(color.getRGB()).tex(0, 1).endVertex();
        builder.pos(matrices, x + width, y + height, 0).color(color.getRGB()).tex(1, 1).endVertex();
        builder.pos(matrices, x + width, y, 0).color(color.getRGB()).tex(1, 0).endVertex();
        tessellator.draw();

        RenderSystem.bindTexture(0);
        RenderSystem.disableBlend();
    }

    public Vector3d cameraPos() {
        return mc.gameRenderer.getActiveRenderInfo().getProjectedView();
    }

    public void drawCleanImage(MatrixStack stack, double x, double y, double z, double width, double height, FixColor color) {
        drawCleanImage(stack, x, y, z, width, height, color, color, color, color);
    }

    public void drawCleanImage(MatrixStack stack, double x, double y, double z, double width, double height, FixColor color1, FixColor color2, FixColor color3, FixColor color4) {
        Matrix4f matrix = stack.getLast().getMatrix();
        int color1RGB = color1.getRGB();
        int color2RGB = color2.getRGB();
        int color3RGB = color3.getRGB();
        int color4RGB = color4.getRGB();

        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
        builder.pos(matrix, (float) x, (float) (y + height), (float) z).color((color1RGB >> 16) & 0xFF, (color1RGB >> 8) & 0xFF, color1RGB & 0xFF, color1.getAlpha()).tex(0, 1 - 0.01f).lightmap(0, 240).endVertex();
        builder.pos(matrix, (float) (x + width), (float) (y + height), (float) z).color((color2RGB >> 16) & 0xFF, (color2RGB >> 8) & 0xFF, color2RGB & 0xFF, color2.getAlpha()).tex(1, 1 - 0.01f).lightmap(0, 240).endVertex();
        builder.pos(matrix, (float) (x + width), (float) y, (float) z).color((color3RGB >> 16) & 0xFF, (color3RGB >> 8) & 0xFF, color3RGB & 0xFF, color3.getAlpha()).tex(1, 0).lightmap(0, 240).endVertex();
        builder.pos(matrix, (float) x, (float) y, (float) z).color((color4RGB >> 16) & 0xFF, (color4RGB >> 8) & 0xFF, color4RGB & 0xFF, color4.getAlpha()).tex(0, 0).lightmap(0, 240).endVertex();

        tessellator.draw();
    }
    public static void drawSubstring(MatrixStack matrixStack, CFontRenderer font, String text, float x, float y, int color, float maxWidth) {
        substringShader.start();
        substringShader.setFloat("inColor", (float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f, (float)(color >> 24 & 0xFF) / 255.0f);        substringShader.setFloat("width", maxWidth);
        substringShader.setFloat("width", maxWidth);
        substringShader.setFloat("maxWidth", (x + maxWidth) * 2);
        font.draw(matrixStack, text, x, y, color);
        substringShader.finish();
    }
    public void drawImage(MatrixStack matrices, ResourceLocation location, double x, double y, double z, double width, double height, Color colorC) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        mc.getTextureManager().bindTexture(location);

        Matrix4f matrix = matrices.getLast().getMatrix();
        int color = colorC.getRGB();

        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
        builder.pos(matrix, (float) x, (float) (y + height), (float) z).color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, colorC.getAlpha()).tex(0, 1 - 0.01f).lightmap(0, 240).endVertex();
        builder.pos(matrix, (float) (x + width), (float) (y + height), (float) z).color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, colorC.getAlpha()).tex(1, 1 - 0.01f).lightmap(0, 240).endVertex();
        builder.pos(matrix, (float) (x + width), (float) y, (float) z).color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, colorC.getAlpha()).tex(1, 0).lightmap(0, 240).endVertex();
        builder.pos(matrix, (float) x, (float) y, (float) z).color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, colorC.getAlpha()).tex(0, 0).lightmap(0, 240).endVertex();

        tessellator.draw();

        RenderSystem.bindTexture(0);
        RenderSystem.disableBlend();
    }

    public void drawItemStackOverlap(MatrixStack matrixStack, ItemStack itemStack, double x, double y, float scale, int count) {
        matrixStack.push();
        if (count == -1) {
            count = itemStack.getCount();
        }
        String countString = String.valueOf(count);

        {
              matrixStack.translate(0.0D, 0.0D, (double)(mc.getItemRenderer().zLevel + 200.0F));

            Fonts.SEMIBOLD_14.draw(matrixStack, countString, (float) (x + 17 - Fonts.SEMIBOLD_14.width(countString)), (float)(y + 0), -1);
        }
        scaleStart(x, y, scale);
        mc.getItemRenderer().renderItemIntoGUI(itemStack, (int) x, (int) y);


        // mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, (int) x, (int) y, "ignored");
        scaleEnd();
        matrixStack.pop();
    }

    public void drawItemStack(MatrixStack matrixStack, ItemStack itemStack, double x, double y, float scale, int count) {
        matrixStack.push();

        scaleStart(x, y, scale);
        mc.getItemRenderer().renderItemIntoGUI(itemStack, (int) x, (int) y);
        if (count == -1) {
            count = itemStack.getCount();
        }
        String countString = count > 1 ? String.valueOf(count) : "";
        if (count > 1)
        {
            matrixStack.translate(0.0D, 0.0D, (double)(mc.getItemRenderer().zLevel + 200.0F));

            Fonts.SEMIBOLD_14.draw(matrixStack, countString, (float) (x + 19 - 2 - Fonts.SEMIBOLD_14.width(countString)), (float)(y + 6 + 3), -1);
        }

        mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, (int) x, (int) y, "ignored");
        scaleEnd();
        matrixStack.pop();
    }

    public void color(FixColor color) {
        float r = color.getRed() / 255.0F;
        float g = color.getGreen() / 255.0F;
        float b = color.getBlue() / 255.0F;
        float a = color.getAlpha() / 255.0F;
        GlStateManager.color4f(r, g, b, a);
    }

    public void color(int color, float alpha) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GlStateManager.color4f(r, g, b, alpha);
    }

    public void color(int color) {
        color(color, (float) (color >> 24 & 255) / 255.0F);
    }

    public void resetColor() {
        GlStateManager.color4f(1, 1, 1, 1);
    }

    public void setAlphaLimit(float limit) {
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(GL11.GL_GREATER, (float) (limit * .01));
    }

    public void startScissor(double x, double y, double width, double height) {
        final double scale = window.getGuiScaleFactor();
        y = window.getScaledHeight() - y;

        x *= scale;
        y *= scale;
        width *= scale;
        height *= scale;

        RenderSystem.enableScissor((int) x, (int) (y - height), (int) width, (int) height);
    }
    public void endScissor() {
        RenderSystem.disableScissor();
    }

    public Vector2f project(net.minecraft.util.math.vector.Vector3d vec) {
        return project(vec.x, vec.y, vec.z);
    }
    public Vector2f project(double x, double y, double z) {
        Vector3d camera_pos = mc.getRenderManager().info.getProjectedView();
        Quaternion cameraRotation = mc.getRenderManager().getCameraOrientation().copy();
        cameraRotation.conjugate();

        Vector3f result3f = new Vector3f((float) (camera_pos.x - x), (float) (camera_pos.y - y), (float) (camera_pos.z - z));
        result3f.transform(cameraRotation);


        Entity renderViewEntity = mc.getRenderViewEntity();
        if (renderViewEntity instanceof PlayerEntity playerentity) {
            calculateHurtCamera(playerentity, result3f);

            if (mc.gameSettings.viewBobbing) {
                calculateViewBobbing(playerentity, result3f);
            }
        }

        double fov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);

        return calculateScreenPosition(result3f, fov);
    }

    private void calculateViewBobbing(PlayerEntity playerentity, Vector3f result3f) {
        float walked = playerentity.distanceWalkedModified;
        float f = walked - playerentity.prevDistanceWalkedModified;
        float f1 = -(walked + f * mc.getRenderPartialTicks());
        float f2 = MathHelper.lerp(mc.getRenderPartialTicks(), playerentity.prevCameraYaw, playerentity.cameraYaw);

        Quaternion quaternion = new Quaternion(Vector3f.XP, Math.abs(MathHelper.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F, true);
        quaternion.conjugate();
        result3f.transform(quaternion);

        Quaternion quaternion1 = new Quaternion(Vector3f.ZP, MathHelper.sin(f1 * (float) Math.PI) * f2 * 3.0F, true);
        quaternion1.conjugate();
        result3f.transform(quaternion1);

        Vector3f bobTranslation = new Vector3f((MathHelper.sin(f1 * (float) Math.PI) * f2 * 0.5F), (-Math.abs(MathHelper.cos(f1 * (float) Math.PI) * f2)), 0.0f);
        bobTranslation.setY(-bobTranslation.getY());
        result3f.add(bobTranslation);
    }
    private Vector2f calculateScreenPosition(Vector3f result3f, double fov) {
        float halfHeight = mc.getMainWindow().getScaledHeight() / 2.0F;
        float scaleFactor = halfHeight / (result3f.getZ() * (float) Math.tan(Math.toRadians(fov / 2.0F)));
        AspectRatio aspectRatio = Client.getInstance().getModuleManager().get(AspectRatio.class);
        if (result3f.getZ() < 0.0F) {
            return new Vector2f(-result3f.getX() * scaleFactor / (aspectRatio.isEnabled() ? aspectRatio.getAspect().getValue() : 1) + mc.getMainWindow().getScaledWidth() / 2.0F, mc.getMainWindow().getScaledHeight() / 2.0F - result3f.getY() * scaleFactor);
        }
        return new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
    }

    public Vector3d interpolate(Entity entity, float partialTicks) {
        double posX = Maths.lerp(entity.lastTickPosX, entity.getPosX(), partialTicks);
        double posY = Maths.lerp(entity.lastTickPosY, entity.getPosY(), partialTicks);
        double posZ = Maths.lerp(entity.lastTickPosZ, entity.getPosZ(), partialTicks);
        return new Vector3d(posX, posY, posZ);
    }

    private void calculateHurtCamera(PlayerEntity playerentity, Vector3f result3f) {
        NoRender module = Client.getInstance().getModuleManager().get(NoRender.class);
        if (module.isEnabled() && module.remove.get("Тряска при ударе").isEnabled())
            return;


        if (playerentity != null)
        {
            float f = (float)playerentity.hurtTime - mc.getRenderPartialTicks();

            if (playerentity.getShouldBeDead())
            {
                float f1 = Math.min((float)playerentity.deathTime + mc.getRenderPartialTicks(), 20.0F);
                Quaternion quaternion = new Quaternion(Vector3f.ZP, 40.0F - 8000.0F / (f1 + 200.0F), true);
                quaternion.conjugate();
                result3f.transform(quaternion);
            }

            if (f < 0.0F)
            {
                return;
            }

            f = f / (float)playerentity.maxHurtTime;
            f = MathHelper.sin(f * f * f * f * (float)Math.PI);
            float f2 = playerentity.attackedAtYaw;
            Quaternion quaternion2 = new Quaternion(Vector3f.YP, -f2, true);
            quaternion2.conjugate();
            result3f.transform(quaternion2);

            Quaternion quaternion3 = new Quaternion(Vector3f.ZP, -f * 14.0F, true);
            quaternion3.conjugate();
            result3f.transform(quaternion3);

            Quaternion quaternion4 = new Quaternion(Vector3f.YP, f2, true);
            quaternion4.conjugate();
            result3f.transform(quaternion4);
        }
    }


    public int loadTexture(BufferedImage image) {
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        ByteBuffer buffer = BufferUtils.createByteBuffer(pixels.length * 4);

        try {
            for (int pixel : pixels) {
                buffer.put((byte)((pixel >> 16) & 0xFF));
                buffer.put((byte)((pixel >> 8) & 0xFF));
                buffer.put((byte)(pixel & 0xFF));
                buffer.put((byte)((pixel >> 24) & 0xFF));
            }
            buffer.flip();
        } catch (BufferOverflowException | ReadOnlyBufferException ex) {return -1;}

        int textureID = GlStateManager.genTexture();
        GlStateManager.bindTexture(textureID);
        GlStateManager.texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
        GlStateManager.texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
        GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, buffer);
        GlStateManager.bindTexture(0);

        return textureID;
    }

    public void drawRect(MatrixStack matrix, float x, float y, float width, float height, FixColor color) {
        float maxX = x + width;
        float maxY = y + height;

        float f3 = color.getAlpha() / 255.0F;
        float f = color.getRed() / 255.0F;
        float f1 = color.getGreen() / 255.0F;
        float f2 = color.getBlue() / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(matrix.getLast().getMatrix(), x, maxY, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(matrix.getLast().getMatrix(), maxX, maxY, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(matrix.getLast().getMatrix(), maxX, y, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(matrix.getLast().getMatrix(), x, y, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }


    public void grid(MatrixStack matrixStack, FixColor color, float step) {
        step = Math.max(1, step);

        for (int x = 0; x < window.getScaledWidth(); x += step) {
            drawRect(matrixStack, x, 0, 1, window.getScaledHeight(), color);
        }
        for (int y = 0; y < window.getScaledWidth(); y += step) {
            drawRect(matrixStack,0, y, window.getScaledWidth(), 1, color);
        }
    }

    public void gridMask(MatrixStack matrixStack, FixColor color, Rect rect, float step, float offset) {
        Stencil.init();
        Render.grid(matrixStack, FixColor.WHITE, step);
        Stencil.read(1);
        Glow.draw(matrixStack, rect.size(-1), offset, color.getAlpha(), 10 + offset, color, color, color, color);
        Stencil.finish();
    }

    public void drawBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        FixColor first = FixColor.BLACK;
        drawRect(matrixStack,0, 0, window.getScaledWidth(), window.getScaledHeight(), first);

        int step = 25;
        Render.grid(matrixStack, TempColor.getBackgroundColor().alpha(100.0D), step);
        Render.gridMask(matrixStack, FixColor.WHITE.alpha(15D), new Rect(mouseX - 5, mouseY - 5, 10, 10), step, 50 + step * 2);
    }

    public void outline(MatrixStack ms, Rect rect, float round, float height, float width, FixColor color) {
        outline(ms, rect, round, height, width, color, color, color, color);
    }

    public void outline(MatrixStack ms, Rect rect, float round, float height, float width, FixColor color, FixColor color2, FixColor color3, FixColor color4) {
        Outline.draw(ms, rect.y(rect.getY()).size(height), round, width, color, color2, color3, color4);
    }

    public boolean isInView(Entity ent) {
        if (mc.getRenderViewEntity() == null ||mc.getRenderManager().info == null) return false;
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x, mc.getRenderManager().info.getProjectedView().y,mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(ent.getBoundingBox()) || ent.ignoreFrustumCheck;
    }

    public boolean isInView(Vector3d vec) {
        assert mc.getRenderViewEntity() != null;
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x, mc.getRenderManager().info.getProjectedView().y,mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(new AxisAlignedBB(vec.add(-0.5,-0.5, -0.5), vec.add(0.5,0.5, 0.5)));
    }

    public boolean isInView(double x, double y, double z) {
        return isInView(new Vector3d(x, y, z));
    }

    public boolean isInView(AxisAlignedBB box) {
        if (mc.getRenderViewEntity() == null) {
            return false;
        }
        return frustum.isBoundingBoxInFrustum(box);
    }

    public void drawEntity3D(MatrixStack ms, LivingEntity player, Vector3d pos, float alpha) {
        ActiveRenderInfo activeRenderInfo = mc.gameRenderer.getActiveRenderInfo();

     //   GlowESP.SILENT_RENDERING = true;

        ms.push();

        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture();
        GL11.glShadeModel(7425);
        GlStateManager.disableCull();
        GlStateManager.enableDepthTest();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA);
        GlStateManager.glBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA.param, GlStateManager.DestFactor.ONE.param, GlStateManager.SourceFactor.ZERO.param, GlStateManager.DestFactor.ONE.param);
        GL11.glEnable(GL11.GL_DEPTH_TEST);


        Vector3d cameraPos = mc.getRenderManager().info.getProjectedView();
        try {
            double x = pos.x - mc.getRenderManager().info.getProjectedView().getX(),
                    y = pos.y - mc.getRenderManager().info.getProjectedView().getY(),
                    z = pos.z - mc.getRenderManager().info.getProjectedView().getZ();

            EntityRendererManager renderManager = mc.getRenderManager();

            if (renderManager == null)
                return;

            float partialTicks = mc.getRenderPartialTicks();

            Vector3d camera = activeRenderInfo.getProjectedView();

            //Reacher.ENTITY_ALPHA = alpha;
           // Reacher.SILENT = true;


            if (Config.isShaders()) {
                Shaders.nextEntity(player);
            }

            renderManager.renderEntityStatic(
                    player,
                    x,
                    y,
                    z,
                    MathHelper.lerp(partialTicks, 0, 0),
                    partialTicks, ms,
                    mc.getRenderTypeBuffers().getBufferSource(),
                    renderManager.getPackedLight(player, partialTicks)
            );

           // Reacher.ENTITY_ALPHA = 1;
            //Reacher.SILENT = false;
        } catch (Exception e1) {
            e1.printStackTrace();
        }


        GlStateManager.blendFunc(770, 771);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        ms.pop();
      //  GlowESP.SILENT_RENDERING = false;
    }

}
