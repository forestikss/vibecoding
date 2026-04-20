package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

public abstract class ListScreenListBase<T extends ListScreenEntryBase<T>> extends AbstractOptionList<T> {

    public ListScreenListBase(int width, int height, int top, int size) {
        super(Minecraft.getInstance(), width, height, top, top + height, size);
    }

    public void updateSize(int width, int height, int top) {
        updateSize(width, height, top, top + height);
    }

    protected void renderList2(MatrixStack p_238478_1_, int p_238478_2_, int p_238478_3_, int p_238478_4_, int p_238478_5_, float p_238478_6_)
    {
        int i = this.getItemCount();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        for (int j = 0; j < i; ++j)
        {
            int k = this.getRowTop(j);
            int l = this.getRowBottom(j);

            if (l >= this.y0 && k <= this.y1)
            {
                int i1 = p_238478_3_ + j * this.itemHeight + this.headerHeight;
                int j1 = this.itemHeight - 4;
                T e = this.getEntry(j);
                int k1 = this.getRowWidth();

                if (this.renderSelection && this.isSelectedItem(j))
                {
                    int l1 = this.x0 + this.width / 2 - k1 / 2;
                    int i2 = this.x0 + this.width / 2 + k1 / 2;
                    RenderSystem.disableTexture();
                    float f = this.isFocused() ? 1.0F : 0.5F;
                    RenderSystem.color4f(f, f, f, 1.0F);
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
                    bufferbuilder.pos((double)l1, (double)(i1 + j1 + 2), 0.0D).endVertex();
                    bufferbuilder.pos((double)i2, (double)(i1 + j1 + 2), 0.0D).endVertex();
                    bufferbuilder.pos((double)i2, (double)(i1 - 2), 0.0D).endVertex();
                    bufferbuilder.pos((double)l1, (double)(i1 - 2), 0.0D).endVertex();
                    tessellator.draw();
                    RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
                    bufferbuilder.pos((double)(l1 + 1), (double)(i1 + j1 + 1), 0.0D).endVertex();
                    bufferbuilder.pos((double)(i2 - 1), (double)(i1 + j1 + 1), 0.0D).endVertex();
                    bufferbuilder.pos((double)(i2 - 1), (double)(i1 - 1), 0.0D).endVertex();
                    bufferbuilder.pos((double)(l1 + 1), (double)(i1 - 1), 0.0D).endVertex();
                    tessellator.draw();
                    RenderSystem.enableTexture();
                }

                int j2 = this.getRowLeft();
                e.render(p_238478_1_, j, k, j2, k1, j1, p_238478_4_, p_238478_5_, this.isMouseOver((double)p_238478_4_, (double)p_238478_5_) && Objects.equals(this.getEntryAtPosition((double)p_238478_4_, (double)p_238478_5_), e), p_238478_6_);
            }
        }
    }

    @Override
    public void render(MatrixStack poseStack, int x, int y, float partialTicks) {
        double scale = minecraft.getMainWindow().getGuiScaleFactor();
        int scaledHeight = minecraft.getMainWindow().getScaledHeight();
        RenderSystem.enableScissor(0, (int) ((double) (scaledHeight - y1) * scale), Integer.MAX_VALUE / 2, (int) ((double) height * scale));
        {
            int j1 = this.getRowLeft();
            int k = this.y0 + 4 - (int)this.getScrollAmount();
            this.renderList2(poseStack, j1, k, x, y, partialTicks);
        }
        RenderSystem.disableScissor();
    }

}
