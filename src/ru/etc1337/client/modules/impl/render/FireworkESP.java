package ru.etc1337.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Data;
import lombok.experimental.Accessors;
import net.minecraft.item.Items;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.packet.EventReceivePacket;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.ui.dropui.AnimationMath;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.render.ui.api.Header;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Firework ESP", description = "Отображает ESP на позиции использованных фейерверков в мире", category = ModuleCategory.RENDER)
public class FireworkESP extends Module {
    private final SliderSetting lifeTime = new SliderSetting("Время Жизни", this, 50, 10, 100, 1);

    private final List<Firework> fireworks = new CopyOnWriteArrayList<>();

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventReceivePacket receivePacket) {
            if (receivePacket.getPacket() instanceof SPlaySoundEffectPacket packet) {
                if (packet.getCategory() == SoundCategory.AMBIENT && packet.getSound() == SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH) {
                    fireworks.add(new Firework(new Vector3d(packet.getX(), packet.getY(), packet.getZ())));
                }
            }
        }
        if (event instanceof EventRender2D eventRender2D) {
            if (fireworks.isEmpty()) return;

            fireworks.removeIf(firework -> firework.timer.finished((long) (lifeTime.getValue() * 100)));

            for (Firework firework : fireworks) {
                firework.updatePosition();

                Vector3d interpolatedPos = firework.getInterpolatedPosition(mc.getRenderPartialTicks());
                Vector2f screenPos = Render.project(interpolatedPos.x, interpolatedPos.y, interpolatedPos.z);

                if (screenPos.x == Float.MAX_VALUE || screenPos.y == Float.MAX_VALUE) continue;

                float width = -10;
                String timeText = "firework";
                float timeWidth = Fonts.SEMIBOLD_14.width(timeText) + 5;
                float maxWidth = Math.max(width, timeWidth) / 2;

                float x = screenPos.x - maxWidth / 2;
                float y = screenPos.y + 5;
                float stackSize = 8;
                float size = (stackSize / 2F);

                Header.drawModernHeader(eventRender2D.getMatrixStack(), null, (int)x - 16, (int)y, Items.FIREWORK_ROCKET.getDefaultInstance(), timeText, 0.85f);

             /*   FixColor backgroundColor = TempColor.getBackgroundColor();
                Header.drawBackground(eventRender2D.getMatrixStack(), x - 5, y - 3f, 10.5f, timeWidth - 0.5f, 9.5f,
                        backgroundColor, backgroundColor.alpha(225));
                Fonts.SEMIBOLD_14.draw(eventRender2D.getMatrixStack(), timeText, x - 5 + 12.5f, y - 2.5f, TempColor.getFontColor().getRGB());


                Render.scaleStart(x + (stackSize / 2F), y + (stackSize / 2F), 0.5F);
                RenderSystem.translated((x - stackSize - size), (y - stackSize), 0);
                mc.getItemRenderer().renderItemAndEffectIntoGUI(Items.FIREWORK_ROCKET.getDefaultInstance(), 0, 0);
                RenderSystem.translated(-(x - stackSize - size), -(y - stackSize), 0);
                Render.scaleEnd();*/
            }
        }
    }

    @Override
    public void onDisable() {
        fireworks.clear();
        super.onDisable();
    }

    @Data
    @Accessors(chain = true)
    public static class Firework {
        private Vector3d position;
        private final Timer timer = new Timer();
        private final List<Vector3d> positionHistory = new ArrayList<>();
        private static final int MAX_HISTORY = 3;

        public Firework(Vector3d position) {
            this.position = position;
            positionHistory.add(position);
        }

        public void updatePosition() {
            positionHistory.add(position);
            if (positionHistory.size() > MAX_HISTORY) {
                positionHistory.remove(0);
            }
        }

        public Vector3d getInterpolatedPosition(float partialTicks) {
            if (positionHistory.size() < 2) return position;

            Vector3d latest = positionHistory.get(positionHistory.size() - 1);
            Vector3d previous = positionHistory.get(positionHistory.size() - 2);

            double x = AnimationMath.lerp((float) previous.x, (float) latest.x, partialTicks);
            double y = AnimationMath.lerp((float) previous.y, (float) latest.y, partialTicks);
            double z = AnimationMath.lerp((float) previous.z, (float) latest.z, partialTicks);

            return new Vector3d(x, y, z);
        }
    }
}