package ru.etc1337.client.commands.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.handler.EventListener;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.client.commands.Command;
import ru.etc1337.client.commands.api.CommandInfo;
import ru.etc1337.client.commands.api.CommandParameter;
import ru.kotopushka.compiler.sdk.annotations.Compile;

@CommandInfo(name = "Gps", description = "asd", aliases = {"way", "gps"})
public final class GpsCommand extends Command implements EventListener {
    private BlockPos gpsPosition;

    CommandParameter clear = new CommandParameter(this, "off", "clear");

    public GpsCommand() {
        Client.getEventManager().register(this);
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            sendErrorMessage();
            return;
        }

        if (this.contains(args[1], clear)) {
            gpsPosition = null;
        } else {
            loadLocation(args);
        }
    }

    private double calculateAngleToTarget() {
        double x = gpsPosition.getX() - mc.getRenderManager().info.getProjectedView().getX();
        double z = gpsPosition.getZ() - mc.getRenderManager().info.getProjectedView().getZ();
        float yawRadians = (float) Math.toRadians(mc.getRenderManager().info.getYaw());
        double cos = MathHelper.cos(yawRadians);
        double sin = MathHelper.sin(yawRadians);
        double rotY = -(z * cos - x * sin);
        double rotX = -(x * cos + z * sin);
        return Math.toDegrees(Math.atan2(rotY, rotX));
    }

    private void drawArrow(double angle, EventRender2D e) {
        double posX = window.getScaledWidth() / 2.0;
        double posY = 160;
        float arrowSize = 1f;
        float width = 19 * arrowSize;
        float height = 21 * arrowSize;

        GlStateManager.pushMatrix();
        GlStateManager.disableBlend();
        GlStateManager.translated(posX, posY, 0.0);
        GlStateManager.rotatef((float) (angle + 90.0), 0.0f, 0.0f, 1.0f);
        FixColor color = TempColor.getClientColor();
        Render.drawImage(e.getMatrixStack(), new ResourceLocation("minecraft", "dreamcore/images/util/arrow.png"), -width / 2, -height / 2, width, height, color);
        GlStateManager.enableBlend();
        GlStateManager.popMatrix();

        Fonts.SEMIBOLD_14.drawCenter(e.getMatrixStack(), "До точки: " + getDistance(gpsPosition) + "m", window.getScaledWidth() / 2F, 170, -1);
    }


    public int getDistance(BlockPos pos) {
        if (mc.player == null) return -1;

        double x = mc.player.getPosX() - pos.getX();
        double z = mc.player.getPosZ() - pos.getZ();
        return (int) MathHelper.sqrt((float) (x * x + z * z));
    }

    private void loadLocation(String[] args) {
        if (args.length != 3) {
            sendErrorMessage();
            return;
        }

        try {
            int x = Integer.parseInt(args[1]);
            int z = Integer.parseInt(args[2]);
            gpsPosition = new BlockPos(x, 0, z);
            Chat.send(TextFormatting.GRAY + "Метка была поставлена на координаты: " + x + ", " + z);
        } catch (NumberFormatException e) {
            Chat.send(TextFormatting.RED + "Неправильные координаты. Просьба ввести в формате x, z.");
        }
    }

    @Compile
    private void sendErrorMessage() {
        error();
        Chat.send(TextFormatting.GRAY + ".gps x, z");
        Chat.send(TextFormatting.GRAY + ".gps off");
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRender2D eventRender2D) {
            if (mc.player == null || mc.world == null || gpsPosition == null) {
                return;
            }

            if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
                drawArrow(calculateAngleToTarget(), eventRender2D);
            }
        }
    }
}