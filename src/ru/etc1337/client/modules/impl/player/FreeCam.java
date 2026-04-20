package ru.etc1337.client.modules.impl.player;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerAbilitiesPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.game.EventWorldChanged;
import ru.etc1337.api.events.impl.packet.EventReceivePacket;
import ru.etc1337.api.events.impl.packet.EventSendPacket;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.game.Move;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

import java.util.UUID;

@ModuleInfo(name = "Free Cam",description = "Позволяет визуально перемещаться",category = ModuleCategory.PLAYER)
public class FreeCam extends Module {
    private final SliderSetting speedXZ = new SliderSetting("Скорость по XZ", this, 1, 0.1f, 3, 0.1f);
    private final SliderSetting speedY = new SliderSetting("Скорость по Y", this, 1, 0.1f, 3, 0.1f);

    private float x,y,z;
    @Getter
    RemoteClientPlayerEntity ent;

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRender2D eventRender2D) {
            int xPosition = (int) (mc.player.getPosX() - x), yPosition = (int) (mc.player.getPosY() - y), zPosition = (int) (mc.player.getPosZ() - z);
            String pos = "X: " + xPosition + " Y: " + yPosition + " Z: " + zPosition;
            Fonts.SEMIBOLD_16.draw(eventRender2D.getMatrixStack(), pos, window.getScaledWidth() / 2f - 28.0f, window.getScaledHeight() / 2f - 20.0f, TempColor.getFontColor().getRGB());
        }
        if (event instanceof EventSendPacket eventSendPacket) {
            IPacket<?> packet = eventSendPacket.getPacket();

            if (packet instanceof CPlayerPacket || packet instanceof CPlayerAbilitiesPacket) event.setCancelled(true);
        }
        if (event instanceof EventReceivePacket eventReceivePacket) {
            IPacket<?> packet = eventReceivePacket.getPacket();

            if (packet instanceof SPlayerPositionLookPacket) {
                event.setCancelled(true);
            }
        }
        if (event instanceof EventWorldChanged eventWorldChanged) {
            this.toggle();
        }
        if (event instanceof EventUpdate eventUpdate) {
            if (mc.currentScreen instanceof DeathScreen) {
                this.toggle();
            }
            if (!mc.player.isSneaking() && mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.player.getMotion().y = this.speedY.getValue();
            } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.player.getMotion().y = -this.speedY.getValue();
            } else {
                mc.player.setMotion(0.0, 0.0, 0.0);
            }

            Move.setMotion(this.speedXZ.getValue());
        }
    }

    @Override
    public void onEnable() {
        if (mc.player != null && mc.world != null) {
            this.x = (float) mc.player.getPosX();
            this.y = (float) mc.player.getPosY();
            this.z = (float) mc.player.getPosZ();
            this.ent = new RemoteClientPlayerEntity(mc.world, new GameProfile(UUID.randomUUID(), mc.getSession().getUsername()));
            ent.inventory = mc.player.inventory;
            ent.setHealth(mc.player.getRealHealth());
            ent.setPositionAndRotation(this.x, mc.player.getBoundingBox().minY, this.z, mc.player.rotationYaw, mc.player.rotationPitch);
            ent.rotationYawHead = mc.player.rotationYawHead;
            mc.world.addEntity(-1337, ent);
            mc.player.abilities.isFlying = true;
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.player != null && mc.world != null) {
            mc.player.setMotion(0.0, 0.0, 0.0);
            mc.player.setVelocity(0.0, 0.0, 0.0);
            if (this.x != 0 && this.y != 0 && this.z != 0) {
                mc.player.setPosition(this.x, this.y, this.z);
            }
            if (ent != null) {
                ent.remove();
                ent = null;
            }
            mc.world.removeEntityFromWorld(-1337);
            mc.player.abilities.isFlying = false;
        }
        super.onDisable();
    }
}
