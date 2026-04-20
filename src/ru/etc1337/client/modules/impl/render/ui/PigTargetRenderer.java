package ru.etc1337.client.modules.impl.render.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.LivingEntity;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.render.EventRenderWorldEntities;
import ru.etc1337.client.modules.impl.combat.KillAura;
import ru.etc1337.client.modules.impl.render.ui.api.ElementInfo;
import ru.etc1337.client.modules.impl.render.ui.api.UIElement;

@ElementInfo(name = "Pig", initX = 8.0F, initY = 8.0F, initHeight = 17.0F)
public class PigTargetRenderer extends UIElement {

    private net.minecraft.entity.passive.PigEntity pigInstance = null;
    private static final float SPEED = 1.5f;

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventRenderWorldEntities e)) return;

        KillAura killAura = Client.getInstance().getModuleManager().get(KillAura.class);
        LivingEntity target = killAura.getTarget();
        if (target == null && !(mc.pointedEntity instanceof LivingEntity)) return;
        if (target == null) target = (LivingEntity) mc.pointedEntity;

        drawPigs(e, target);
    }

    private void drawPigs(EventRenderWorldEntities e, LivingEntity target) {
        float partialTicks = e.getPartialTicks();
        double tx = target.lastTickPosX + (target.getPosX() - target.lastTickPosX) * partialTicks - e.getX();
        double ty = target.lastTickPosY + (target.getPosY() - target.lastTickPosY) * partialTicks - e.getY();
        double tz = target.lastTickPosZ + (target.getPosZ() - target.lastTickPosZ) * partialTicks - e.getZ();

        float скрст = 0.00025f * SPEED;
        float времс = -(float)(System.currentTimeMillis() % 1000000) * скрст;
        float радс = 0.7f;
        float высота = 1f;

        double[] px = new double[8];
        double[] py = new double[8];
        double[] pz = new double[8];

        float aoe = времс * 360;
        for (int i = 0; i < 8; i++) {
            float та = aoe + (i / 8.0f) * 360f;
            double rad = Math.toRadians(та);
            float вОф = (i % 2 == 0) ? 0.1f : -0.1f;
            px[i] = tx + Math.cos(rad) * радс;
            py[i] = ty + высота + вОф - 0.2f;
            pz[i] = tz + Math.sin(rad) * радс;
        }

        float времяДр = (float)(System.currentTimeMillis() % 1000000) * SPEED * 0.001f;
        float яв   = времяДр * 180;
        float питч = (float)(Math.sin(времяДр * 1.5) * 120);
        float крут = (float)(Math.cos(времяДр * 1.2) * 90);

        double свX = tx, свY = ty + 2.2f, свZ = tz;
        MatrixStack ms = e.getMatrix();

        if (pigInstance == null || pigInstance.world == null) {
            try { pigInstance = new net.minecraft.entity.passive.PigEntity(net.minecraft.entity.EntityType.PIG, mc.world); }
            catch (Exception ex) { return; }
        }
        pigInstance.limbSwing = 0; pigInstance.limbSwingAmount = 0;
        pigInstance.prevLimbSwingAmount = 0; pigInstance.ticksExisted = 0;

        for (int i = 0; i < 9; i++) {
            double мX = i < 8 ? px[i] : свX;
            double мY = i < 8 ? py[i] : свY;
            double мZ = i < 8 ? pz[i] : свZ;

            ms.push();
            ms.translate(мX, мY, мZ);

            if (i == 8) {
                ms.rotate(net.minecraft.util.math.vector.Vector3f.YP.rotationDegrees(яв));
                ms.rotate(net.minecraft.util.math.vector.Vector3f.XP.rotationDegrees(питч));
                ms.rotate(net.minecraft.util.math.vector.Vector3f.ZP.rotationDegrees(крут));
            } else {
                int next = (i + 1) % 8;
                double lX = px[next] - px[i], lZ = pz[next] - pz[i];
                float yaw = (float)Math.toDegrees(Math.atan2(-lZ, lX)) - 95;
                ms.rotate(net.minecraft.util.math.vector.Vector3f.YP.rotationDegrees(yaw));
            }

            float кес = (i == 8) ? 0.4f : 0.3f;
            ms.scale(кес, кес, кес);

            try {
                mc.getRenderManager().renderEntityStatic(
                        pigInstance, 0, 0, 0, 0, partialTicks, ms,
                        e.getVertex(),
                        mc.getRenderManager().getPackedLight(pigInstance, partialTicks));
            } catch (Exception ex) { /* ignore */ }

            ms.pop();
        }
    }
}
