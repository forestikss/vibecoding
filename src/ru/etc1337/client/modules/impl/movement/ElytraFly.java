package ru.etc1337.client.modules.impl.movement;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerAbilitiesPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.vector.Vector2f;
import org.lwjgl.glfw.GLFW;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.*;
import ru.etc1337.api.events.impl.input.EventInputKey;
import ru.etc1337.api.events.impl.packet.EventReceivePacket;
import ru.etc1337.api.events.impl.packet.EventSendPacket;
import ru.etc1337.api.game.*;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.combat.KillAura;

@ModuleInfo(name = "Elytra Fly", description = "Полет на элитре", category = ModuleCategory.MOVEMENT)
public class ElytraFly extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", this, "Fireworks");

    private final Timer swapTimer = new Timer();

    @Override
    public void onEvent(Event event) {
        if (mode.is("Fireworks")) {
            if (event instanceof EventUpdate eventUpdate) {
                if (!mc.gameSettings.keyBindJump.isKeyDown()) mc.gameSettings.keyBindJump.setPressed(true);
            }
            if (event instanceof EventMove) {
                //if (event instanceof EventMoveFix eventMoveFix) eventMoveFix.setPitch(0); // - Strafes
                //Player.look(event, new Vector2f(mc.player.rotationYaw, -44), 1); - High Jump
                int fireworkSlot = Inventory.findItem(Items.FIREWORK_ROCKET);
                if (fireworkSlot == -1) {
                    Chat.send("Для работы Elytra Fly нужны фейерверки.");
                    this.toggle();
                    return;
                }

                int i = Inventory.findItem(Items.ELYTRA);
                if (i == -1) {
                    Chat.send("Для работы Elytra Fly нужна элитра.");
                    this.toggle();
                    return;
                }

                if (i > 9) {
                    int hotbarSlot = 8;
                    hotbarSlot = hotbarSlot + 36;
                    Inventory.moveItem(i, hotbarSlot);
                }

                if (!mc.player.isOnGround() && mc.player.fallDistance > 0 && !mc.player.isInWater() && !mc.player.isInLava()) {
                    if (swapTimer.finished(500)) {
                        Player.swapElytra(i);
                        Inventory.Use.useItem(fireworkSlot, false, false, 7, 0, 0);
                        swapTimer.reset();
                    }
                }
            }
        }
        if (mc.player != null && mc.player.isElytraFlying()) {
            KillAura killAura = Client.getInstance().getModuleManager().get(KillAura.class);
            float yaw = killAura.getTarget() != null ? killAura.getRotationVector().x : mc.player.rotationYaw;
            float pitch = killAura.getTarget() != null ? killAura.getRotationVector().y : mc.player.rotationPitch;
            Player.look(event, new Vector2f(yaw, pitch), Player.Correction.FULL, null);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}