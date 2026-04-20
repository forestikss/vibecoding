package ru.etc1337.client.modules.impl.movement;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.inventory.AnvilScreen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventInventoryClose;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.packet.EventSendPacket;
import ru.etc1337.api.game.Inventory;
import ru.etc1337.api.game.Move;
import ru.etc1337.api.other.ScriptConstructor;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.combat.KillAura;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Gui Move", description = "Ходьба в гуи", category = ModuleCategory.MOVEMENT)
public class GuiMove extends Module {
    public final BooleanSetting bypass = new BooleanSetting("Обход кликов", this);
    public final BooleanSetting bypassS = new BooleanSetting("Обход свапов", this);
    public final BooleanSetting bypassSS = new BooleanSetting("Обход использования", this);
    private final Timer timer = new Timer();
    private final List<IPacket<?>> packet = new ArrayList<>();
    private static final ScriptConstructor script = new ScriptConstructor();

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (bypass.isEnabled()  && mc.currentScreen instanceof ChestScreen) return;

            KeyBinding[] pressedKeys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump, mc.gameSettings.keyBindSprint};

            if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof EditSignScreen || mc.currentScreen instanceof AnvilScreen) return;
            updateKeyBindingState(pressedKeys);
        }
        if (event instanceof EventSendPacket eventPacket) {
            if (bypass.isEnabled() && eventPacket.getPacket() instanceof CClickWindowPacket windowPacket && Move.isMoving()
                    && mc.currentScreen instanceof InventoryScreen) {
                packet.add(windowPacket); // накапливаем
                eventPacket.setCancelled(true);
            }
        }

        if (event instanceof EventInventoryClose eventInventoryClose) {
            AutoSprint autoSprint = Client.getInstance().getModuleManager().get(AutoSprint.class);
            if (bypass.isEnabled() && !packet.isEmpty() && mc.currentScreen instanceof InventoryScreen) {
                if (Inventory.Use.script.isFinished()) {
                    Inventory.Use.script.cleanup()
                            .addStep(0, () -> {
                                autoSprint.setCanSprint(false);
                            })
                            .addTickStep(1, () -> {
                                packet.forEach(packet -> mc.player.connection.sendPacket(packet));
                                packet.clear();
                                mc.player.connection.sendPacket(new CCloseWindowPacket());
                                autoSprint.setCanSprint(!Client.getInstance().getModuleManager().get(KillAura.class).legitSprint.isEnabled());
                            });

                    eventInventoryClose.setCancelled(true);
                }
            }
        }
    }
    private void updateKeyBindingState(KeyBinding[] keyBindings) {
        for (KeyBinding keyBinding : keyBindings) {
            boolean isKeyPressed = InputMappings.isKeyDown(window.getHandle(), keyBinding.getDefault().getKeyCode());
            keyBinding.setPressed(isKeyPressed);
        }
    }
}
