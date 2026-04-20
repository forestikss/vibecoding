package ru.etc1337.client.modules.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.animations.simple.Animation;
import ru.etc1337.api.animations.simple.Easing;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.render.Render;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.combat.AntiBot;

import java.awt.*;
import java.util.regex.Pattern;

@ModuleInfo(name = "Arrows", description = "Рисует стрелочки до игроков", category = ModuleCategory.RENDER)
public class Arrows extends Module {
    private final Animation animationStep = new Animation(Easing.SINE_IN_OUT, 500L);
    private final Pattern NAME_REGEX = Pattern.compile("^[A-zА-я0-9_]{3,16}$");

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRender2D eventRender2D) {
            if (mc.player == null || mc.world == null || mc.getRenderManager() == null) return;

            float size = mc.currentScreen instanceof ContainerScreen screen ? screen.ySize : 80;
            animationStep.update(size);

            mc.world.getPlayers().stream()
                    .filter(player -> isNameValid(player.getNameClear()) && mc.player != player && !AntiBot.isBot(player))
                    .forEach(player -> {
                        double[] pos = calculatePlayerScreenPosition(player);
                        drawArrow(eventRender2D.getMatrixStack(), pos[0], pos[1], pos[2], player);
                    });
        }
    }
    private double[] calculatePlayerScreenPosition(AbstractClientPlayerEntity player) {
        double x = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getX();
        double z = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * mc.getRenderPartialTicks() - mc.getRenderManager().info.getProjectedView().getZ();

        float yawRadians = (float) Math.toRadians(mc.getRenderManager().info.getYaw());
        double cos = MathHelper.cos(yawRadians);
        double sin = MathHelper.sin(yawRadians);

        double rotY = -(z * cos - x * sin);
        double rotX = -(x * cos + z * sin);

        float angle = (float) Math.toDegrees(Math.atan2(rotY, rotX));
        double x2 = animationStep.getValue() * MathHelper.cos((float) Math.toRadians(angle)) + window.getScaledWidth() / 2.0f;
        double y2 = animationStep.getValue() * MathHelper.sin((float) Math.toRadians(angle)) + window.getScaledHeight() / 2.0f;

        return new double[]{x2, y2, angle};
    }

    private void drawArrow(MatrixStack ms, double x, double y, double angle, PlayerEntity player) {
        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, 0.0);
        GlStateManager.rotatef((float) (angle + 90.0f), 0.0f, 0.0f, 1.0f);

        Color color = Client.getInstance().getFriendManager().isFriend(player.getGameProfile().getName())
                ? new Color(32, 255, 32, 255) : TempColor.getClientColor();
        Render.drawImage(ms, new ResourceLocation("minecraft", "dreamcore/images/util/arrow.png"), -6.0f, 6.0f, 12, 14, color);

        GlStateManager.popMatrix();
    }

    public boolean isNameValid(String name) {
        return NAME_REGEX.matcher(name).matches();
    }
}