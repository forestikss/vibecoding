package ru.etc1337.client.modules.impl.combat;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.game.Inventory;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@ModuleInfo(name = "Auto Totem", description = "Автоматически берет тотем", category = ModuleCategory.COMBAT)
public class AutoTotem extends Module {

    // Константы
    private static final int SWAP_DELAY = 400;
    private static final String[] BALL_KEYWORDS = {"шар", "голова", "head"};

    // Настройки
    private final SliderSetting healthThreshold = new SliderSetting("Здоровье", this, 10, 1F, 20, 1);
    private final SliderSetting healthElytraThreshold = new SliderSetting("Здоровье на Elytra", this, 10, 1F, 20, 1);
    private final BooleanSetting swapBack = new BooleanSetting("Возвращать предмет", this);
    private final BooleanSetting saveEnchanted = new BooleanSetting("Сохранять зачарованный", this);
    private final BooleanSetting noBallSwitch = new BooleanSetting("Не брать если шар", this);
    private final BooleanSetting swapDelay = new BooleanSetting("Задержка свапа", this);

    private final MultiModeSetting mode = new MultiModeSetting("Учитывать", this,
            "Поглощение", "Обсидиан", "Кристалл", "Якорь", "Падение");

    private final SliderSetting obsidianRadius = new SliderSetting("Радиус от обсидиона", this, 16, 1, 32, 1)
            .setVisible(() -> mode.get("Обсидиан").isEnabled());
    private final SliderSetting crystalRadius = new SliderSetting("Радиус от кристалла", this, 16, 1, 32, 1)
            .setVisible(() -> mode.get("Кристалл").isEnabled());
    private final SliderSetting anchorRadius = new SliderSetting("Радиус от якоря", this, 16, 1, 32, 1)
            .setVisible(() -> mode.get("Якорь").isEnabled());
    private final SliderSetting fallDistance = new SliderSetting("Дистанция падения", this, 16, 1, 32, 1)
            .setVisible(() -> mode.get("Падение").isEnabled());

    // Состояние
    private int previousItemSlot = -1;
    private ItemStack previousItemStack = ItemStack.EMPTY;
    private final Timer swapTimer = new Timer();
    private int cachedNonEnchantedTotems = 0;

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventUpdate) || !Inventory.Use.script.isFinished() ||  (swapDelay.isEnabled() && !swapTimer.finished(SWAP_DELAY))) {
            return;
        }

        updateTotemCount();

        if (shouldEquipTotem()) {
            equipTotem();
        } else if (shouldSwapBack()) {
            swapBackToPreviousItem();
        }
    }

    private void updateTotemCount() {
        cachedNonEnchantedTotems = (int) mc.player.openContainer.inventorySlots.stream()
                .filter(slot -> isNonEnchantedTotem(slot.getStack()))
                .count();
    }

    private boolean shouldEquipTotem() {
        return isInDanger() && !hasTotemInHands() && findAvailableTotem().isPresent();
    }

    private boolean shouldSwapBack() {
        return previousItemSlot != -1 && swapBack.isEnabled() && !isInDanger() && isPreviousItemStillValid();
    }

    private void equipTotem() {
        findAvailableTotem().ifPresent(slot -> {
            if (!mc.player.getHeldItemOffhand().isEmpty() && previousItemSlot == -1) {
                storePreviousItem(slot.slotNumber);
            }
            performSwap(slot.slotNumber, true);
        });
    }

    private void swapBackToPreviousItem() {
        if (isPreviousItemStillValid()) {
            performSwap(previousItemSlot, false);
        } else {
            resetPreviousItem();
        }
    }

    private Optional<Slot> findAvailableTotem() {
        return mc.player.openContainer.inventorySlots.stream()
                .filter(slot -> slot.getStack().getItem() == Items.TOTEM_OF_UNDYING)
                .filter(slot -> isValidTotemForUse(slot.getStack()))
                .findFirst();
    }

    private boolean isValidTotemForUse(ItemStack itemStack) {
        return !saveEnchanted.isEnabled() || !itemStack.isEnchanted() || cachedNonEnchantedTotems <= 0;
    }

    private boolean isNonEnchantedTotem(ItemStack itemStack) {
        return itemStack.getItem() == Items.TOTEM_OF_UNDYING && !itemStack.isEnchanted();
    }

    private void storePreviousItem(int slotNumber) {
        previousItemSlot = slotNumber;
        previousItemStack = mc.player.getHeldItemOffhand().copy();
    }

    private void resetPreviousItem() {
        previousItemSlot = -1;
        previousItemStack = ItemStack.EMPTY;
    }

    private boolean isPreviousItemStillValid() {
        if (previousItemSlot == -1) return false;

        Slot returnSlot = mc.player.openContainer.inventorySlots.get(previousItemSlot);
        return returnSlot != null &&
                !returnSlot.getStack().isEmpty() &&
                returnSlot.getStack().getItem() == previousItemStack.getItem();
    }

    private void performSwap(int slotNumber, boolean isEquippingTotem) {
        Inventory.swapHand(slotNumber, Hand.OFF_HAND);
        swapTimer.reset();

        if (!isEquippingTotem) {
            resetPreviousItem();
        }
    }

    private boolean isInDanger() {
        return isHealthLow() || (!isHoldingBall() && (isCrystalNearby() || isAnchorNearby() || isObsidianNearby() || isFallDamageRisk()));
    }

    private boolean isHealthLow() {
        float currentHealth = mc.player.getHealth();
        if (mode.get("Поглощение").isEnabled()) {
            currentHealth += mc.player.getAbsorptionAmount();
        }
        float healthThresholdValue = mc.player.isElytraFlying() || mc.player.inventory.getStackInSlot(38).getItem() == Items.ELYTRA
                ? healthElytraThreshold.getValue() : healthThreshold.getValue();
        return healthThresholdValue >= currentHealth;
    }

    private boolean isHoldingBall() {
        if (!noBallSwitch.isEnabled()) return false;

        ItemStack offhandItem = mc.player.getHeldItemOffhand();
        if (offhandItem.getItem() instanceof SkullItem) return true;

        String displayName = offhandItem.getDisplayName().getString().toLowerCase();
        for (String keyword : BALL_KEYWORDS) {
            if (displayName.contains(keyword)) return true;
        }
        return false;
    }

    private boolean isCrystalNearby() {
        if (!mode.get("Кристалл").isEnabled()) return false;

        double radiusSquared = crystalRadius.getValue() * crystalRadius.getValue();
        List<Entity> entities = new ArrayList<>();
        mc.world.getAllEntities().forEach(entities::add);

        return entities.stream()
                .anyMatch(entity -> entity instanceof EnderCrystalEntity &&
                        mc.player.getDistanceSq(entity) <= radiusSquared);
    }

    private boolean isAnchorNearby() {
        if (!mode.get("Якорь").isEnabled()) return false;
        return findNearestBlock(anchorRadius.getValue(), Blocks.RESPAWN_ANCHOR).isPresent();
    }

    private boolean isObsidianNearby() {
        if (!mode.get("Обсидиан").isEnabled()) return false;
        return findNearestBlock(obsidianRadius.getValue(), Blocks.OBSIDIAN).isPresent();
    }

    private boolean isFallDamageRisk() {
        return mode.get("Падение").isEnabled() && mc.player.fallDistance >= fallDistance.getValue();
    }

    private Optional<BlockPos> findNearestBlock(float radius, net.minecraft.block.Block targetBlock) {
        return getSpherePositions(mc.player.getPosition(), radius, 6, false, true, 0)
                .stream()
                .filter(pos -> mc.world.getBlockState(pos).getBlock() == targetBlock)
                .min(Comparator.comparing(pos -> getDistanceToBlock(mc.player, pos)));
    }

    private boolean hasTotemInHands() {
        for (Hand hand : Hand.values()) {
            ItemStack heldItem = mc.player.getHeldItem(hand);
            if (heldItem.getItem() == Items.TOTEM_OF_UNDYING && isValidTotemForUse(heldItem)) {
                return true;
            }
        }
        return false;
    }

    private List<BlockPos> getSpherePositions(BlockPos center, float radius, int height, boolean hollow, boolean semiHollow, int yOffset) {
        List<BlockPos> positions = new ArrayList<>();
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();

        int minX = centerX - (int) radius;
        int maxX = centerX + (int) radius;
        int minZ = centerZ - (int) radius;
        int maxZ = centerZ + (int) radius;

        float radiusSquared = radius * radius;
        float innerRadiusSquared = (radius - 1.0f) * (radius - 1.0f);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int minY = semiHollow ? (centerY - (int) radius) : centerY;
                int maxY = semiHollow ? (centerY + (int) radius) : (centerY + height);

                for (int y = minY; y < maxY; y++) {
                    double distanceSquared = calculateDistanceSquared(centerX, centerY, centerZ, x, y, z, semiHollow);

                    if (distanceSquared < radiusSquared && (!hollow || distanceSquared >= innerRadiusSquared)) {
                        positions.add(new BlockPos(x, y + yOffset, z));
                    }
                }
            }
        }
        return positions;
    }

    private double calculateDistanceSquared(int centerX, int centerY, int centerZ, int x, int y, int z, boolean includeY) {
        double dx = centerX - x;
        double dz = centerZ - z;
        double dy = includeY ? (centerY - y) : 0;
        return dx * dx + dy * dy + dz * dz;
    }

    private double getDistanceToBlock(Entity entity, BlockPos blockPos) {
        double dx = entity.getPosX() - blockPos.getX();
        double dy = entity.getPosY() - blockPos.getY();
        double dz = entity.getPosZ() - blockPos.getZ();
        return MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
    }
}