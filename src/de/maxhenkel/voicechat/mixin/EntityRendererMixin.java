package de.maxhenkel.voicechat.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.events.RenderEvents;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;

public abstract class EntityRendererMixin {

    private void renderNameTag(Entity entity, ITextComponent component, MatrixStack poseStack, IRenderTypeBuffer multiBufferSource, int light) {
        if (!entity.getDisplayName().equals(component)) {
            return;
        }
        RenderEvents.RENDER_NAMEPLATE.invoker().render(entity, component, poseStack, multiBufferSource, light);
    }

}
