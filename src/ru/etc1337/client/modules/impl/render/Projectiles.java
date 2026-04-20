package ru.etc1337.client.modules.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.events.impl.render.EventRender3D;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.render.ui.api.Header;

import java.util.Locale;

@Getter
@ModuleInfo(name = "Projectiles", description = "Траектория падения предметов", category = ModuleCategory.RENDER)
public class Projectiles extends Module {

    private final MultiModeSetting projectiles = new MultiModeSetting("Projectiles",
            this, "Эндер Жемчуг",
            "Стрела",
            "Трезубец");
  //  private final BooleanSetting renderName = new BooleanSetting("Показать отправителя", this);

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRender2D eventRender2D) {
            for (Entity entity : mc.world.getAllEntities()) {
                if (validEntity(entity) && noMove(entity)) {
                    Item item = entity instanceof EnderPearlEntity ? Items.ENDER_PEARL : entity instanceof ArrowEntity ? Items.ARROW : Items.TRIDENT;
                    Entity shooter = ((ProjectileEntity) entity).getShooter();
                    String shooterName = shooter != null ? shooter.getName().getString() : "Неизвестно";

                    Vector3d pearlPosition = entity.getPositionVec();
                    Vector3d pearlMotion = entity.getMotion();
                    Vector3d lastPosition = new Vector3d(0, 0, 0);
                    double ticks = 0;

                    for (int i = 0; i <= 300; i++) {
                        lastPosition = pearlPosition;
                        pearlPosition = pearlPosition.add(pearlMotion);
                        pearlMotion = updatePearlMotion(entity, pearlMotion, pearlPosition);
                        ticks++;

                        if (shouldEntityHit(pearlPosition, lastPosition) || pearlPosition.y <= 0) {
                            break;
                        }
                    }

                    double seconds = Math.round((ticks / 20.0) * 10) / 10.0;
                    String timeText = String.format(Locale.ROOT, "%.1fs", seconds);

                    Vector2f position = Render.project(lastPosition.x, lastPosition.y, lastPosition.z);
                    if (position.x == Float.MAX_VALUE && position.y == Float.MAX_VALUE) return;

                    float x = position.x;
                    float y = position.y + 5;
                    float stackSize = 8;
                    float size = (stackSize / 2F);

          /*          FixColor backgroundColor = TempColor.getBackgroundColor();
                    Header.drawBackground(eventRender2D.getMatrixStack(), x - 5, y - 3f, 10.5f, timeWidth - 0.5f, 9.5f,
                            backgroundColor, backgroundColor.alpha(225));
                    Fonts.SEMIBOLD_14.draw(eventRender2D.getMatrixStack(), timeText, x - 5 + 12.5f, y - 2.5f, TempColor.getFontColor().getRGB());
*/
                    Header.drawModernHeader(eventRender2D.getMatrixStack(), null, (int)x - 16, (int)y, item.getDefaultInstance(), timeText, 0.85f);


                /*    if (renderName.isEnabled()) {
                        Round.draw(eventRender2D.getMatrixStack(), new Rect(x - 5, y - 2 + 10, 5 + width, 9.5f), 2, backgroundColor.alpha(225));
                        Fonts.SEMIBOLD_14.draw(eventRender2D.getMatrixStack(), shooterName, x - 5 + 2, y - 2 + 10f, TempColor.getFontColor().getRGB());
                    }
*/

             /*       Render.scaleStart(x + (stackSize / 2F), y + (stackSize / 2F), 0.5F);
                    RenderSystem.translated((x - stackSize - size), (y - stackSize), 0);
                    mc.getItemRenderer().renderItemAndEffectIntoGUI(item.getDefaultInstance(), 0, 0);
                    RenderSystem.translated(-(x - stackSize - size), -(y - stackSize), 0);
                    Render.scaleEnd();*/
                }
            }
        }
        if (event instanceof EventRender3D eventRender3D) {
            MatrixStack matrix = eventRender3D.getMatrixStack();

            RenderSystem.pushMatrix();
            RenderSystem.multMatrix(matrix.getLast().getMatrix());
            RenderSystem.translated(-mc.getRenderManager().renderPosX(), -mc.getRenderManager().renderPosY(), -mc.getRenderManager().renderPosZ());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();
            RenderSystem.disableDepthTest();
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            RenderSystem.lineWidth(1.5F);
            RenderSystem.color4f(1f, 1f, 1f, 1f);
            builder.begin(1, DefaultVertexFormats.POSITION_COLOR);
            for (Entity entity : mc.world.getAllEntities()) {
                if (validEntity(entity) && noMove(entity))
                    renderLine(entity);
            }
            tessellator.draw();
            RenderSystem.enableDepthTest();
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            RenderSystem.translated(mc.getRenderManager().renderPosX(), mc.getRenderManager().renderPosY(), mc.getRenderManager().renderPosZ());
            RenderSystem.popMatrix();
        }
    }

    private void renderLine(Entity pearl) {
        Vector3d pearlPosition = pearl.getPositionVec().add(0, 0, 0);
        Vector3d pearlMotion = pearl.getMotion();
        Vector3d lastPosition;
        for (int i = 0; i <= 300; i++) {
            lastPosition = pearlPosition;
            pearlPosition = pearlPosition.add(pearlMotion);
            pearlMotion = updatePearlMotion(pearl, pearlMotion, lastPosition);

            if (shouldEntityHit(pearlPosition, lastPosition) || pearlPosition.y <= 0) {
                break;
            }

            int color = FixColor.fade(i * 5);
            builder.pos(lastPosition.x, lastPosition.y, lastPosition.z).color(color).endVertex();
            builder.pos(pearlPosition.x, pearlPosition.y, pearlPosition.z).color(color).endVertex();
        }
    }

    public Vector3d updatePearlMotion(Entity entity, Vector3d originalPearlMotion, Vector3d pearlPosition) {
        Vector3d pearlMotion = originalPearlMotion;

        if ((entity.isInWater() || mc.world.getBlockState(new BlockPos(pearlPosition)).getBlock() == Blocks.WATER) && !(entity instanceof TridentEntity)) {
            float scale = entity instanceof EnderPearlEntity ? 0.8f : 0.6f;
            pearlMotion = pearlMotion.scale(scale);
        } else {
            pearlMotion = pearlMotion.scale(0.99f);
        }

        if (!entity.hasNoGravity())
            pearlMotion.y -= entity instanceof EnderPearlEntity ? 0.03 : 0.05;

        return pearlMotion;
    }

    public boolean shouldEntityHit(Vector3d pearlPosition, Vector3d lastPosition) {
        final RayTraceContext rayTraceContext = new RayTraceContext(
                lastPosition,
                pearlPosition,
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                mc.player
        );
        final BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);

        return blockHitResult.getType() == RayTraceResult.Type.BLOCK;
    }

    boolean noMove(Entity entity) {
        return entity.prevPosY != entity.getPosY() || entity.prevPosX != entity.getPosX() || entity.prevPosZ != entity.getPosZ();
    }

    boolean validEntity(Entity entity) {
        return (entity instanceof EnderPearlEntity && projectiles.get("Эндер Жемчуг").isEnabled())
                || (entity instanceof ArrowEntity && projectiles.get("Стрела").isEnabled())
                || (entity instanceof TridentEntity && projectiles.get("Трезубец").isEnabled());
    }
}