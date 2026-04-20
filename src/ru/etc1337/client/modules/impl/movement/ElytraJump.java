package ru.etc1337.client.modules.impl.movement;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventFireworkMotion;
import ru.etc1337.api.events.impl.game.EventInputMove;
import ru.etc1337.api.events.impl.game.EventMove;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.game.*;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.combat.KillAura;
import ru.kotopushka.compiler.sdk.annotations.Compile;

@ModuleInfo(name = "Elytra Jump", description = "Полет вверх на элитре", category = ModuleCategory.MOVEMENT)
public class ElytraJump extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", this, "Fireworks", "Exploit");

    private final BooleanSetting auto = new BooleanSetting("Авто", this).setVisible(() -> mode.is("Exploit"));
    private final BooleanSetting bypass = new BooleanSetting("Bypass", this).setVisible(() -> mode.is("Exploit"));
    private final Timer timer = new Timer();

    @Override
    public void onEvent(Event event) {
        if (mode.is("Exploit")) {
            if (event instanceof EventUpdate) {
                if (mc.player.hurtTime > 0) {
                    if (auto.isEnabled() && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem().equals(Items.ELYTRA)) {
                        swap(false);
                    }
                    return;
                }
            }
            if (event instanceof EventInputMove eventInputMove) {
                if (mc.player != null && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem().equals(Items.ELYTRA)) {
                    eventInputMove.setForward(0);
                    eventInputMove.setStrafe(0);
                }
            }
            if (mc.player != null && mc.player.movementInput != null) {
                if (mc.player.inventory.getStackInSlot(38).getItem() == Items.ELYTRA && mc.player.movementInput.jump &&
                        !mc.player.isElytraFlying()) {
                    mc.player.movementInput.jump = false;
                }
            }
            if (event instanceof EventUpdate) {
                if (mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem().equals(Items.ELYTRA)) {
                    Move.setMotion(0);
                    mc.gameSettings.keyBindJump.setPressed(true);
                    if (mc.player.isElytraFlying()) {
                        if (timer.finished(500) && bypass.isEnabled()) {
                            Inventory.Use.use(Items.FIREWORK_ROCKET, false, true);
                            timer.reset();
                        }
                        mc.player.getMotion().y += Maths.randomNew(0.06f, 0.061f);
                    }
                } else {
                    if (auto.isEnabled()) {
                        swap(false);
                    }
                }
            }
            if (event instanceof EventFireworkMotion eventFireworkMotion && bypass.isEnabled()) {
                eventFireworkMotion.setVector3d(Vector3d.ZERO);
                eventFireworkMotion.setCancelled(true);
            }
            if (mc.player != null && mc.player.isElytraFlying()) {
                KillAura killAura = Client.getInstance().getModuleManager().get(KillAura.class);
                float yaw = killAura.getTarget() != null ? killAura.getRotationVector().x : mc.player.rotationYaw;
                Player.look(event, new Vector2f(yaw, 0), Player.Correction.FULL, null);
            }

        } else if (mode.is("Fireworks")) {
            if (event instanceof EventUpdate eventUpdate) {
                if (!mc.gameSettings.keyBindJump.isKeyDown()) mc.gameSettings.keyBindJump.setPressed(true);
            }
            if (event instanceof EventMove) {
                //if (event instanceof EventMoveFix eventMoveFix) eventMoveFix.setPitch(0); // - Strafes
                //Player.look(event, new Vector2f(mc.player.rotationYaw, -44), 1); - High Jump
                int fireworkSlot = Inventory.findItem(Items.FIREWORK_ROCKET);
                if (fireworkSlot == -1) {
                    Chat.send("Для работы Elytra Jump нужны фейерверки.");
                    this.toggle();
                    return;
                }

                int i = Inventory.findItem(Items.ELYTRA);
                if (i == -1) {
                    Chat.send("Для работы Elytra Jump нужна элитра.");
                    this.toggle();
                    return;
                }

                if (i > 9) {
                    int hotbarSlot = 8;
                    hotbarSlot = hotbarSlot + 36;
                    Inventory.moveItem(i, hotbarSlot);
                }

                if (!mc.player.isOnGround() && mc.player.fallDistance > 0 && !mc.player.isInWater() && !mc.player.isInLava()) {
                    if (timer.finished(500)) {
                        Player.swapElytra(i);
                        Inventory.Use.useItem(fireworkSlot, false, false, 7, 0, 0);
                        timer.reset();
                    }
                }
            }
            if (mc.player != null && mc.player.isElytraFlying()) {
                KillAura killAura = Client.getInstance().getModuleManager().get(KillAura.class);
                float yaw = killAura.getTarget() != null ? killAura.getRotationVector().x : mc.player.rotationYaw;
                Player.look(event, new Vector2f(yaw, -90), Player.Correction.FULL, null);
            }
        }
    }

    @Compile
    @Override
    public void onDisable() {
        if (mode.is("Exploit")) {
            if (auto.isEnabled()) {
                if (mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem().equals(Items.ELYTRA)) {
                    swap(true);
                }
            }
        }
        super.onDisable();
    }

    @Compile
    private void swap(boolean chestplate) {
        if (mode.is("Exploit")) {
            int chestPlate = chestplate ? Inventory.findChestPlate() : Inventory.findItem(Items.ELYTRA);

            if (chestPlate != -1) {
                Inventory.moveItem(chestPlate, 6);
            }
        }
    }
}