package ru.etc1337.client.modules.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Totem Animation", description = "Анимация при снесении тотема", category = ModuleCategory.RENDER)
public class TotemAnimation extends Module {

    public final ModeSetting mode = new ModeSetting("Анимация", this, "Fade Out", "Size", "Insert", "Fall", "Rocket", "Roll");
    private final SliderSetting speed = new SliderSetting("Скорость", this, 40, 1, 100, 0.1f);


    private ItemStack floatingItem = null;
    private int floatingItemTimeLeft;


    public void displayItemActivation(ItemStack floatingItem) {
        this.floatingItem = floatingItem;
        floatingItemTimeLeft = getTime();
    }
    
    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate eventUpdate) {
            if (floatingItemTimeLeft > 0) {
                --floatingItemTimeLeft;
                if (floatingItemTimeLeft == 0) {
                    floatingItem = null;
                }
            }
        }
    }


    public void renderItemActivation(MatrixStack matrixStack, float partialTicks) {
        if (floatingItem != null && this.floatingItemTimeLeft > 0) {
            int scaledWidth = window.getScaledWidth();
            int scaledHeight = window.getScaledHeight();

            RenderSystem.enableAlphaTest();
            RenderSystem.pushMatrix();
            RenderSystem.pushLightingAttributes();
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            int elapsedTime = getTime() - floatingItemTimeLeft;
            float animationProgress = ((float) elapsedTime + partialTicks) / (float) getTime();
            float progressSquared = animationProgress * animationProgress;
            float progressCubed = animationProgress * progressSquared;
            float oscillationFactor = 10.25F * progressCubed * progressSquared - 24.95F * progressSquared * progressSquared + 25.5F * progressCubed - 13.8F * progressSquared + 4.0F * animationProgress;
            float oscillationRadians = oscillationFactor * 3.1415927F;
            matrixStack.push();
            float adjustedProgress = ((float) elapsedTime + partialTicks);
            float scale = 50.0F + 175.0F * MathHelper.sin(oscillationRadians);

            switch (mode.getCurrentMode()) {
                case "Fade Out" -> {
                    final float x2 = (float) (Math.sin(((adjustedProgress * 112) / 180f)) * 100);
                    final float y2 = (float) (Math.cos(((adjustedProgress * 112) / 180f)) * 50);
                    matrixStack.translate((float) (scaledWidth / 2) + x2, (float) (scaledHeight / 2) + y2, -50.0F);
                    matrixStack.scale(scale, -scale, scale);
                }

                case "Size" -> {
                    matrixStack.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2), -50.0F);
                    matrixStack.scale(scale, -scale, scale);
                }

                case "Insert" -> {
                    matrixStack.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2), -50.0F);
                    matrixStack.multiply(Vector3f.XP.rotationDegrees(adjustedProgress * 3));
                    matrixStack.scale(200 - adjustedProgress * 1.5f, -200 + adjustedProgress * 1.5f, 200 - adjustedProgress * 1.5f);
                }

                case "Fall" -> {
                    float downFactor = (float) (Math.pow(adjustedProgress, 3) * 0.2f);
                    matrixStack.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2) + downFactor, -50.0F);
                    matrixStack.multiply(Vector3f.ZP.rotationDegrees(adjustedProgress * 5));
                    matrixStack.scale(200 - adjustedProgress * 1.5f, -200 + adjustedProgress * 1.5f, 200 - adjustedProgress * 1.5f);
                }

                case "Rocket" -> {
                    float downFactor = (float) (Math.pow(adjustedProgress, 3) * 0.2f) - 20;
                    matrixStack.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2) - downFactor, -50.0F);
                    matrixStack.multiply(Vector3f.YP.rotationDegrees(adjustedProgress * floatingItemTimeLeft * 2));
                    matrixStack.scale(200 - adjustedProgress * 1.5f, -200 + adjustedProgress * 1.5f, 200 - adjustedProgress * 1.5f);
                }

                case "Roll" -> {
                    float rightFactor = (float) (Math.pow(adjustedProgress, 2) * 4.5f);
                    matrixStack.translate((float) (scaledWidth / 2) + rightFactor, (float) (scaledHeight / 2), -50.0F);
                    matrixStack.multiply(Vector3f.ZP.rotationDegrees(adjustedProgress * 40));
                    matrixStack.scale(200 - adjustedProgress * 1.5f, -200 + adjustedProgress * 1.5f, 200 - adjustedProgress * 1.5f);
                }
            }


            IRenderTypeBuffer.Impl irendertypebuffer$impl = mc.getRenderTypeBuffers().getBufferSource();
            this.mc.getItemRenderer().renderItem(floatingItem, ItemCameraTransforms.TransformType.FIXED, 15728880, OverlayTexture.NO_OVERLAY, matrixStack, irendertypebuffer$impl);
            matrixStack.pop();

            irendertypebuffer$impl.finish();
            RenderSystem.popAttributes();
            RenderSystem.popMatrix();
            RenderSystem.enableCull();
            RenderSystem.disableDepthTest();
        }
    }

    private int getTime() {
        int invertedSpeed = (int) (101 - speed.getValue());

        if (mode.is("Fade Out"))
            return invertedSpeed / 4;

        if (mode.is("Insert"))
            return invertedSpeed / 2;

        return invertedSpeed;
    }

}
