package ru.etc1337.api.game;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.handler.EventListener;
import ru.etc1337.api.events.impl.game.EventInputMove;
import ru.etc1337.api.events.impl.game.EventMotion;
import ru.etc1337.api.events.impl.game.EventTick;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.other.ScriptConstructor;
import ru.etc1337.client.modules.impl.combat.KillAura;
import ru.etc1337.client.modules.impl.movement.AutoSprint;
import ru.etc1337.client.modules.impl.movement.GuiMove;

import java.util.List;

@UtilityClass
public class Inventory implements QuickImports {
    private final Item[] ARMOR_ITEMS = {
            Items.NETHERITE_CHESTPLATE,
            Items.DIAMOND_CHESTPLATE,
            Items.IRON_CHESTPLATE,
            Items.LEATHER_CHESTPLATE,
            Items.GOLDEN_CHESTPLATE,
            Items.CHAINMAIL_CHESTPLATE
    };
    public Slot getSlot(Item item) {
        return mc.player.openContainer.inventorySlots.stream()
                .filter(s -> !mc.player.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem().equals(item))
                .filter(s -> s.getStack().getItem().equals(item))
                .findFirst()
                .orElse(null);
    }
    public int findItem(Item item) {
        return mc.player.inventory.mainInventory.stream()
                .filter(s -> !mc.player.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem().equals(item))
                .filter(s -> s.getItem().equals(item))
                .findFirst()
                .map(mc.player.inventory.mainInventory::indexOf)
                .orElse(-1);
    }

    public int findChestPlate() {
        Item[] items = {Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.LEATHER_CHESTPLATE};
        for (Item item : items) {
            int slot = Inventory.findItem(item);
            if (slot != -1) {
                return slot;
            }
        }
        return -1;
    }
    public boolean hasAirSlotInInventory() {
        for (ItemStack itemStack : mc.player.inventory.mainInventory) {
            if (itemStack.getItem() instanceof AirItem) {
                return true;
            }
        }
        return false;
    }
    public int findArmorSlot() {
        for (Item armorItem : ARMOR_ITEMS) {
            if (mc.player.inventory.getItemStack().getItem() == armorItem) {
                return -1;
            }
            int slot = findItem(armorItem);
            if (slot != -1) {
                return slot;
            }
        }
        return -2;
    }
    public int getAirSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof AirItem) {
                return i;
            }
        }
        return -1;
    }

    public void moveItem(int from, int to) {
        int slot = from < 9 ? from + 36 : from;
        doWindowClick(() -> {
            mc.playerController.windowClick(0, slot, 0, ClickType.SWAP, mc.player);
            mc.playerController.windowClick(0, to, 0, ClickType.SWAP, mc.player);
            mc.playerController.windowClick(0, slot, 0, ClickType.SWAP, mc.player);
        });
    }
    public void swapHand(int slot, Hand hand) {
        doWindowClick(() -> {
            int button = hand.equals(Hand.MAIN_HAND) ? mc.player.inventory.currentItem : 40;
            mc.playerController.windowClick(0, slot, button, ClickType.SWAP, mc.player);
        });
    }

    public void doWindowClick(Runnable runnable) {
        doWindowClick(runnable, true);
    }

    public void doWindowClick(Runnable runnable, boolean sendClose) {
        AutoSprint autoSprint = Client.getInstance().getModuleManager().get(AutoSprint.class);
        boolean legitSwap = Client.getInstance().getModuleManager().get(GuiMove.class).bypass.isEnabled();
        if (!legitSwap) {
            runnable.run();
            return;
        }
        if (!mc.player.isSprinting()) {
            runnable.run();
            if (sendClose) {
                mc.player.connection.sendPacket(new CCloseWindowPacket());
            }
        } else {
            doAction(() -> autoSprint.setCanSprint(false), () -> {
                if (!mc.player.isSprinting()) {
                    runnable.run();
                    if (sendClose) {
                        mc.player.connection.sendPacket(new CCloseWindowPacket());
                    }
                    autoSprint.setCanSprint(!Client.getInstance().getModuleManager().get(KillAura.class).legitSprint.isEnabled());
                }
            });
        }
    }
    public void doAction(Runnable step0, Runnable step1) {
        if (!Use.script.isFinished()) return;
        Use.script.cleanup()
                .addStep(0, step0::run)
                .addTickStep(1, step1::run);
    }


    public static class Use implements EventListener {
        public static final ScriptConstructor script = new ScriptConstructor();
        private static final List<Task> tasks = Lists.newArrayList();

        @Override
        public void onEvent(Event event) {
            if (event instanceof EventUpdate) {
                for (int i = tasks.size() - 1; i >= 0; i--) {
                    final Task task = tasks.get(i);
                    task.stage++;
                    final int hotbarSlot = (mc.player.inventory.currentItem % 8) + 1;
                    useItem(task.slot, task.rotate, task.back, hotbarSlot, mc.player.rotationYaw, mc.player.rotationPitch);
                    tasks.remove(task);
                }
                script.update();
            }
        }

        public static void use(Item item, boolean rotate, boolean back) {
            int slot = Inventory.findItem(item);
            if (slot != -1 && !item.isCooldowned()) {
                tasks.add(new Task(slot, slot <= 8, rotate, back, 0));
            }
        }
        public static void useItem(int itemSlot, boolean rotate, boolean back, int hotbarSlot, float yaw, float pitch) {
            boolean hotbar = itemSlot <= 8;
            GuiMove guiMove = Client.getInstance().getModuleManager().get(GuiMove.class);
            boolean legitSwap = guiMove.bypassS.isEnabled();
            boolean canWork = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.MAIN_HAND || (legitSwap && guiMove.bypassSS.isEnabled());
            if (canWork) {
                int slot = itemSlot <= 8 ? itemSlot + 36 : itemSlot;
                doWindowClick(() -> {
                    mc.playerController.windowClick(0, slot, 40, ClickType.SWAP, mc.player);
                    sendRotationPacket(rotate, yaw, pitch);
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                    mc.playerController.windowClick(0, slot, 40, ClickType.SWAP, mc.player);
                });
            } else {
                if (hotbar) {
                    swapSlot(itemSlot, mc.player.inventory.currentItem);
                    sendRotationPacket(rotate, yaw, pitch);
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                    swapSlot(mc.player.inventory.currentItem, itemSlot);
                } else {
                    doWindowClick(() -> {
                        mc.playerController.windowClick(0, itemSlot, hotbarSlot, ClickType.SWAP, mc.player);
                        swapSlot(hotbarSlot, mc.player.inventory.currentItem);
                        sendRotationPacket(rotate, yaw, pitch);
                        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                        swapSlot(mc.player.inventory.currentItem, hotbarSlot);
                        if (back) mc.playerController.windowClick(0, itemSlot, hotbarSlot, ClickType.SWAP, mc.player);
                    });
                }
            }
        }
        private static void swapSlot(int from, int to) {
            if (from != to) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(from));
            }
        }
        private static void sendRotationPacket(boolean rotate, float yaw, float pitch) {
            if (rotate) {
                mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(
                        yaw,
                        pitch,
                        mc.player.isOnGround()
                ));
            }
        }

        @AllArgsConstructor
        @Getter
        private static class Task {
            private final int slot;
            private final boolean hotbar;
            private final boolean rotate;
            private final boolean back;
            int stage;
        }
    }
}
