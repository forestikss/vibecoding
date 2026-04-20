package ru.etc1337.client.modules.impl.render.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Data;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.animations.simple.Animation;
import ru.etc1337.api.animations.simple.Easing;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventAttack;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.game.Maths;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.shaders.impl.Glow;
import ru.etc1337.api.render.shaders.impl.Outline;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.render.ui.dropui.AnimationMath;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.client.modules.impl.combat.KillAura;
import ru.etc1337.client.modules.impl.misc.NameProtect;
import ru.etc1337.client.modules.impl.render.ui.api.ElementInfo;
import ru.etc1337.client.modules.impl.render.ui.api.Header;
import ru.etc1337.client.modules.impl.render.ui.api.UIElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@ElementInfo(name = "Ghost", initX = 8.0F, initY = 8.0F, initHeight = 17.0F)
public class TargetRenderer extends UIElement {
    private final BooleanSetting absShow = new BooleanSetting("Золотые сердца", this);
    private final BooleanSetting particlesShow = new BooleanSetting("Частицы", this);
    private final BooleanSetting round = new BooleanSetting("Круг", this).setVisible(particlesShow::isEnabled);
    private final BooleanSetting particlesShow2 = new BooleanSetting("Частицы из хп-бара", this).setVisible(particlesShow::isEnabled);
    private final BooleanSetting particlesShow3 = new BooleanSetting("Частицы из головы", this).setVisible(particlesShow::isEnabled);
    private final BooleanSetting markerShow = new BooleanSetting("Маркер", this);
    private final BooleanSetting pigsShow = new BooleanSetting("Свиньи", this);
    private float pigsSpeedValue = 1.5f; // скорость свиней (без SliderSetting — UIElement не Parent)
    private final Animation fadeAnimation = new Animation(Easing.SINE_IN_OUT, 150);
    private final Animation headScaleAnimation = new Animation(Easing.SINE_IN_OUT, 200);
    private LivingEntity target;
    private float health = 0;
    private float abs = 0;
    private final List<HeadParticles> particles = new ArrayList<>();
    private long markerStartTime = 0;

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventAttack && particlesShow.isEnabled() && target != null) {
            if (particlesShow3.isEnabled()) {
                for (int i = 0; i < Maths.randomInt(3, 6); ++i) {
                    particles.add(new HeadParticles(new Vector3d(getDraggable().getX() + 16, getDraggable().getY() + 16, 0)));
                }
            }
            if (particlesShow2.isEnabled()) {
                for (int i = 0; i < Maths.randomInt(1, 3); ++i) {
                    String targetName = target.getName().getString();
                    NameProtect nameProtect = Client.getInstance().getModuleManager().get(NameProtect.class);
                    boolean shouldProtect = Client.getInstance().getFriendManager().isFriend(target) && nameProtect.isEnabled() && nameProtect.friends.isEnabled();
                    if (shouldProtect) {
                        targetName = "stradix";
                    }
                    String targetHealth = String.format("%.1f", target.getRealHealth()).replace(",", ".");
                    float nameWidth = Fonts.SEMIBOLD_14.width(targetName);
                    float healthWidth = MathHelper.clamp(Fonts.SEMIBOLD_14.width(targetHealth), 16f, 64f);

                    float secondPart = nameWidth + 10.5f + healthWidth;
                    final float BAR_WIDTH = secondPart - 5.5f;
                    float hpWidth = BAR_WIDTH * health;
                    particles.add(new HeadParticles(new Vector3d(getDraggable().getX() + 32 + 2f + hpWidth, getDraggable().getY() + 16, 0)));
                }
            }
        }
        if (event instanceof ru.etc1337.api.events.impl.render.EventRenderWorldEntities e3d) {
            handlePigs(e3d);
            return;
        }

        if (!(event instanceof EventRender2D)) return;
        KillAura killAura = Client.getInstance().getModuleManager().get(KillAura.class);
        LivingEntity auraTarget = killAura.getTarget();
        if (auraTarget != null) {
            target = auraTarget;
        } else if (mc.pointedEntity instanceof LivingEntity) {
            target = (LivingEntity) mc.pointedEntity;
        } else if (mc.currentScreen instanceof ChatScreen) {
            target = mc.player;
        }

        fadeAnimation.update(!(mc.currentScreen instanceof ChatScreen) && mc.pointedEntity == null && auraTarget == null ? 0 : 1);

        if (target == null) {
            fadeAnimation.update(0);
            headScaleAnimation.update(0);
            return;
        }

        if (fadeAnimation.getValue() < 0.05F) {
            target = null;
            particles.clear();
            return;
        }

        List<ItemStack> items = new ArrayList<>();

        ItemStack mainStack = target.getHeldItemMainhand();
        if (!mainStack.isEmpty()) {
            items.add(mainStack);
        }

        ItemStack offStack = target.getHeldItemOffhand();
        if (!offStack.isEmpty()) {
            items.add(offStack);
        }

        for (ItemStack itemStack : target.getArmorInventoryList()) {
            if (itemStack.isEmpty()) continue;
            items.add(itemStack);
        }

        MatrixStack matrixStack = ((EventRender2D) event).getMatrixStack();
        float x = getDraggable().getX();
        float y = getDraggable().getY();
        float alpha = fadeAnimation.getValue() * 255;
        FixColor backgroundColor;

        String targetName = target.getName().getString();
        NameProtect nameProtect = Client.getInstance().getModuleManager().get(NameProtect.class);
        boolean shouldProtect = Client.getInstance().getFriendManager().isFriend(target) && nameProtect.isEnabled() && nameProtect.friends.isEnabled();
        if (shouldProtect) {
            targetName = "stradix";
        }
        String targetHealth = String.format("%.1f", target.getRealHealth()).replace(",", ".");
        float nameWidth = Fonts.SEMIBOLD_14.width(targetName);
        float healthWidth = MathHelper.clamp(Fonts.SEMIBOLD_14.width(targetHealth), 16f, 64f);

        final float HEAD_SIZE = 32f;
        float secondPart = nameWidth + 10.5f + healthWidth;
        float totalWidth = HEAD_SIZE + secondPart;
        float height = 32f;

        getDraggable().setHeight(height);
        getDraggable().setWidth(totalWidth);

        // todo: сделать

/*         Header.drawBackground(
                matrixStack,
                (int) x - 0.5F,
                (int) y - 0.5F,
                HEAD_SIZE,
                secondPart,
                height,
                backgroundColor,
                backgroundColor.alpha(225D), new Rect(items.size() < 2 ? 2 : 0, 0, 2.0f, 0), new Rect(0, 2.0f, 0, 2.0f)
        );*/
        backgroundColor = new FixColor(TempColor.getBackgroundColor().darker().darker()).alpha(alpha);
        float roundv = Round.isAMDGPU ? 10 : 8;
        Round.draw(matrixStack, new Rect(x, y, totalWidth, height), roundv, backgroundColor);
        Outline.draw(matrixStack, new Rect(x, y, totalWidth, height), roundv, 1, new FixColor(backgroundColor.alpha(36).brighter()));

        final float HEAD_RECT_SIZE = HEAD_SIZE - 5f;
        float headScale = 1;
        float scaledHeadSize = HEAD_RECT_SIZE * headScale;

        float headOffsetX = (HEAD_RECT_SIZE - scaledHeadSize) / 2;
        float headOffsetY = (HEAD_RECT_SIZE - scaledHeadSize) / 2;

        if (target instanceof PlayerEntity) {
            Round.drawFace(
                    new Rect(x + 2f + headOffsetX, y + 2f + headOffsetY, scaledHeadSize, scaledHeadSize + 1),
                    8,
                    fadeAnimation.getValue(),
                    (AbstractClientPlayerEntity) target
            );
        } else {
            float glowSize = 28;
            Glow.draw(matrixStack, new Rect(
                            x + 10, y + 10f, 12.5f, 12.5f
                    ), glowSize, 1.0f, glowSize / 2,
                    TempColor.getClientColor().alpha(alpha * 0.25f),
                    TempColor.getClientColor().alpha(alpha * 0.25f),
                    TempColor.getClientColor().alpha(alpha * 0.25f),
                    TempColor.getClientColor().alpha(alpha * 0.25f));
            Fonts.SEMIBOLD_28.drawCenter(matrixStack, "?", x + 16, y + 8f, TempColor.getClientColor().alpha(alpha).getRGB());
        }

        if (particlesShow.isEnabled() && (particlesShow3.isEnabled() || particlesShow2.isEnabled())) {
            long currentTime = System.currentTimeMillis();
            Iterator<HeadParticles> iterator = particles.iterator();
            while (iterator.hasNext()) {
                HeadParticles p = iterator.next();
                if (currentTime - p.getTime() > 2000) {
                    iterator.remove();
                    continue;
                }
                p.update(currentTime);
                float particleSize = 4 * p.getScale();
                float halfSize = particleSize / 2;
                if (round.isEnabled()) {
                    Round.draw(
                            matrixStack,
                            new Rect(
                                    (float) (p.getPos().x - halfSize),
                                    (float) (p.getPos().y - halfSize),
                                    particleSize,
                                    particleSize
                            ),
                            2,
                            TempColor.getClientColor().alpha(alpha * 0.5f * p.getAlpha())
                    );
                }
                float glowSize = 10;
                Glow.draw(matrixStack, new Rect(
                                (float) (p.getPos().x - halfSize),
                                (float) (p.getPos().y - halfSize),
                                particleSize,
                                particleSize
                        ), glowSize, 1.0f, glowSize / 2,
                        TempColor.getClientColor().alpha(alpha * 0.5f * p.getAlpha()),
                        TempColor.getClientColor().alpha(alpha * 0.5f * p.getAlpha()),
                        TempColor.getClientColor().alpha(alpha * 0.5f * p.getAlpha()),
                        TempColor.getClientColor().alpha(alpha * 0.5f * p.getAlpha()));
            }
        }


        x += HEAD_SIZE + 2f;
        final float BAR_HEIGHT = 3f;
        final float BAR_Y = y + height / 2f + BAR_HEIGHT;
        final float BAR_WIDTH = secondPart - 5f;

        float healthPercentage = MathHelper.clamp(
                target.getRealHealth() / target.getMaxHealth(),
                0f,
                1f
        );
        health = MathHelper.clamp(
                AnimationMath.fast(health, healthPercentage, 4),
                0f,
                1f
        );

        Round.draw(
                matrixStack,
                new Rect(x, BAR_Y, BAR_WIDTH, BAR_HEIGHT),
                2,
                TempColor.getClientColor().alpha(alpha * 0.1f),
                TempColor.getClientColor().alpha(alpha * 0.2f),
                TempColor.getClientColor().alpha(alpha * 0.1f),
                TempColor.getClientColor().alpha(alpha * 0.2f)
        );

        float hpWidth = BAR_WIDTH * health;
        float hpRounding = hpWidth >= BAR_WIDTH - 1 ? 2 : 0;
        Round.draw(
                matrixStack,
                new Rect(x, BAR_Y, hpWidth, BAR_HEIGHT),
                2, hpRounding, 2, hpRounding,
                TempColor.getClientColor().alpha(alpha)
        );

        if (absShow.isEnabled() && target.getAbsorptionAmount() > 0) {
            float absPercentage = MathHelper.clamp(
                    target.getAbsorptionAmount() / target.getMaxHealth(),
                    0f,
                    1f
            );
            abs = MathHelper.clamp(
                    AnimationMath.fast(abs, absPercentage, 4),
                    0f,
                    1f
            );

            float absWidth = BAR_WIDTH * abs;
            float absRounding = absWidth >= BAR_WIDTH - 1 ? 2 : 0;
            Round.draw(
                    matrixStack,
                    new Rect(x, BAR_Y, absWidth, BAR_HEIGHT),
                    2, absRounding, 2, absRounding,
                    FixColor.YELLOW.alpha(alpha)
            );
        }

        final float TEXT_Y = y + BAR_HEIGHT + 3f;
        Fonts.SEMIBOLD_14.draw(
                matrixStack,
                targetName,
                x,
                TEXT_Y,
                TempColor.getFontColor().alpha(alpha).getRGB()
        );

        Fonts.SEMIBOLD_14.drawCenter(
                matrixStack,
                targetHealth,
                x + nameWidth + 14.5F,
                TEXT_Y,
                TempColor.getClientColor().alpha(alpha).getRGB()
        );


        if (items.size() >= 2 && fadeAnimation.getValue() > 0.15f) {
            x -= HEAD_SIZE + 2.5f;

            float xOffset = 0;
            for (ItemStack item : items) {
                if (item.isEmpty()) continue;

                Render.drawItemStack(matrixStack, item, x + xOffset + 3, y - 10.5F, 0.65f, -5);
                xOffset += 10;
            }
           // Round.draw(matrixStack, new Rect(x, y - 10.5F, xOffset, 10), 2, 2, 0, 0, FixColor.BLACK.alpha(0.55f * alpha));
        }

        // Маркер вокруг цели в мировом пространстве
        if (markerShow.isEnabled() && target != null && !(mc.currentScreen instanceof ChatScreen)) {
            drawMarker(matrixStack, alpha);
        }
    }

    // ---- Свиньи (3D) ----
    private net.minecraft.entity.passive.PigEntity pigInstance = null;

    private void handlePigs(ru.etc1337.api.events.impl.render.EventRenderWorldEntities e) {
        if (!pigsShow.isEnabled() || target == null) return;

        float partialTicks = e.getPartialTicks();
        double tx = target.lastTickPosX + (target.getPosX() - target.lastTickPosX) * partialTicks - e.getX();
        double ty = target.lastTickPosY + (target.getPosY() - target.lastTickPosY) * partialTicks - e.getY();
        double tz = target.lastTickPosZ + (target.getPosZ() - target.lastTickPosZ) * partialTicks - e.getZ();

        float speed = pigsSpeedValue;
        float скрст = 0.00025f * speed;
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

        float времяДр = (float)(System.currentTimeMillis() % 1000000) * speed * 0.001f;
        float яв    = времяДр * 180;
        float питч  = (float)(Math.sin(времяДр * 1.5) * 120);
        float крут  = (float)(Math.cos(времяДр * 1.2) * 90);

        double свX = tx, свY = ty + 2.2f, свZ = tz;

        MatrixStack ms = e.getMatrix();

        if (pigInstance == null || pigInstance.world == null) {
            try { pigInstance = new net.minecraft.entity.passive.PigEntity(net.minecraft.entity.EntityType.PIG, mc.world); } catch (Exception ex) { return; }
        }
        pigInstance.limbSwing = 0; pigInstance.limbSwingAmount = 0; pigInstance.prevLimbSwingAmount = 0; pigInstance.ticksExisted = 0;

        for (int i = 0; i < 9; i++) {
            double мX, мY, мZ;
            if (i < 8) { мX = px[i]; мY = py[i]; мZ = pz[i]; }
            else        { мX = свX;   мY = свY;   мZ = свZ;   }

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
                        mc.getRenderManager().getPackedLight(pigInstance, partialTicks)
                );
            } catch (Exception ex) { /* ignore */ }

            ms.pop();
        }
    }

    private void drawMarker(MatrixStack matrixStack, float alpha) {
        float partialTicks = mc.getRenderPartialTicks();
        double tx = target.lastTickPosX + (target.getPosX() - target.lastTickPosX) * partialTicks;
        double ty = target.lastTickPosY + (target.getPosY() - target.lastTickPosY) * partialTicks;
        double tz = target.lastTickPosZ + (target.getPosZ() - target.lastTickPosZ) * partialTicks;

        if (markerStartTime == 0) markerStartTime = System.currentTimeMillis();
        float elapsed = (System.currentTimeMillis() - markerStartTime) / 200f;

        // синусоидальное смещение
        float bob = (float)(Math.sin(elapsed) * 2f);
        float dist = target.getHeight() / 1.5f + (float)(target.getDistanceSq(mc.player) / 400f);

        // 4 точки вокруг цели (лево, право, верх, низ)
        float size = 12f;
        float gap = 18f + bob;

        // проецируем центр цели
        net.minecraft.util.math.vector.Vector2f center = Render.project(tx, ty + dist, tz);
        if (center.x == Float.MAX_VALUE) return;

        float cx = center.x;
        float cy = center.y;

        FixColor color = TempColor.getClientColor().alpha((int) alpha);

        // Левая стрелка ◄
        drawArrow(matrixStack, cx - gap - size, cy, size, 0, color);
        // Правая стрелка ►
        drawArrow(matrixStack, cx + gap, cy, size, 180, color);
        // Верхняя стрелка ▲
        drawArrow(matrixStack, cx, cy - gap - size, size, 90, color);
        // Нижняя стрелка ▼
        drawArrow(matrixStack, cx, cy + gap, size, 270, color);
    }

    /** Рисует стрелку-маркер в позиции (x,y) повёрнутую на angle градусов */
    private void drawArrow(MatrixStack matrixStack, float x, float y, float size, float angle, FixColor color) {
        matrixStack.push();
        matrixStack.translate(x + size / 2f, y + size / 2f, 0);
        matrixStack.rotate(net.minecraft.util.math.vector.Vector3f.ZP.rotationDegrees(angle));

        // Стрелка из трёх прямоугольников: тело + два уса
        float bodyW = size * 0.35f;
        float bodyH = size * 0.55f;
        float wingW = size * 0.5f;
        float wingH = size * 0.2f;

        // тело
        Render.drawRect(matrixStack, -bodyW / 2f, -bodyH / 2f, bodyW, bodyH, color);
        // левый ус
        Render.drawRect(matrixStack, -wingW / 2f, -bodyH / 2f - wingH + wingH * 0.3f, wingW, wingH, color);

        matrixStack.pop();
    }

    @Data
    static class HeadParticles {
        private static final float MAX_OFFSET = 80f;
        private Vector3d pos;
        private final Vector3d end;
        private final long time;
        private float alpha = 0f;
        private float scale = 0f;

        public HeadParticles(Vector3d start) {
            this.pos = start;
            this.end = start.add(
                    ThreadLocalRandom.current().nextDouble(-MAX_OFFSET, MAX_OFFSET),
                    ThreadLocalRandom.current().nextDouble(-MAX_OFFSET, MAX_OFFSET),
                    0
            );
            this.time = System.currentTimeMillis();
        }

        public void update(long currentTime) {
            long lived = currentTime - time;
            float progress = Math.min(lived / 500f, 1f);

            alpha = MathHelper.clamp(progress * 2, 0f, 1f);
            scale = MathHelper.clamp(progress * 2, 0f, 1f);

            if (lived > 1500) {
                float fadeProgress = (lived - 1500) / 500f;
                alpha = 1f - MathHelper.clamp(fadeProgress, 0f, 1f);
            }

            pos = AnimationMath.fast(pos, end, 0.5f);
        }
    }
}