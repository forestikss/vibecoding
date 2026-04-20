package ru.etc1337.api.game;

import lombok.experimental.UtilityClass;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.*;
import ru.etc1337.api.events.impl.render.EventRender3D;
import ru.etc1337.api.interfaces.QuickImports;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.renderer.WorldRenderer.frustum;

@UtilityClass
public class Player implements QuickImports {
    public double getBps(Entity entity) {
        double x = entity.getPosX() - entity.prevPosX;
        double y = entity.getPosY() - entity.prevPosY;
        double z = entity.getPosZ() - entity.prevPosZ;
        return Math.sqrt((x * x) + (y * y) + (z * z)) * 20.0D;
    }
    public void swapElytra(int slot) {
        // тут обход windowClick не нужен
        mc.playerController.windowClick(0, 6, slot, ClickType.SWAP, mc.player);
        mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
        mc.player.startFallFlying();
        mc.playerController.windowClick(0, 6, slot, ClickType.SWAP, mc.player);
    }
    public Block getBlock() {
        return getBlock(0, 0, 0);
    }
    public static BlockState getBlockState(BlockPos blockPos) {
        return mc.world.getBlockState(blockPos);
    }
    public static Block getBlock(BlockPos blockPos) {
        return Player.getBlockState(blockPos).getBlock();
    }
    public Block getBlock(double x, double y, double z) {
        return mc.player == null ? Blocks.AIR : mc.world.getBlockState(mc.player.getPosition().add(x, y, z)).getBlock();
    }
    public boolean isInWeb() {
        return mc.player.getMotionMultiplier().lengthSquared() > 1.0E-7D;
    }
    public boolean collideWith(LivingEntity entity) {
        return collideWith(entity, 0);
    }

    public boolean isBlockSolid(final double x, final double y, final double z) {
        return Player.block(new BlockPos(x, y, z)).getDefaultState().getMaterial().isSolid();
    }

    public int findItem(final int endSlot, final Item ofType) {
        int slot = -1;

        for (int i = 0; i < endSlot; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() != ofType) continue;
            slot = i == 40 ? 45 : i < 9 ? 36 + i : i;
        }

        return slot;
    }


    public Block block(final BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    public boolean collideWith(LivingEntity entity, float grow) {
        AxisAlignedBB box = mc.player.getBoundingBox();
        AxisAlignedBB targetbox = entity.getBoundingBox().grow(grow, 0, grow);

        if (box.maxX > targetbox.minX
                && box.maxY > targetbox.minY
                && box.maxZ > targetbox.minZ
                && box.minX < targetbox.maxX
                && box.minY < targetbox.maxY
                && box.minZ < targetbox.maxZ) return true;

        return false;
    }

    public List<BlockPos> getCube(final BlockPos center, final float radiusXZ, final float radiusY) {
        List<BlockPos> positions = new ArrayList<>();
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();

        for (int x = centerX - (int) radiusXZ; x <= centerX + radiusXZ; x++) {
            for (int z = centerZ - (int) radiusXZ; z <= centerZ + radiusXZ; z++) {
                for (int y = centerY - (int) radiusY; y < centerY + radiusY; y++) {
                    positions.add(new BlockPos(x, y, z));
                }
            }
        }

        return positions;
    }

    public double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    public Vector2f get(Vector3d target) {
        double posX = target.getX() - mc.player.getPosX();
        double posY = target.getY() - (mc.player.getPosY() + (double) mc.player.getEyeHeight());
        double posZ = target.getZ() - mc.player.getPosZ();
        double sqrt = MathHelper.sqrt(posX * posX + posZ * posZ);
        float yaw = (float) (Math.atan2(posZ, posX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(posY, sqrt) * 180.0 / Math.PI));
        float sens = (float) (Math.pow(mc.gameSettings.mouseSensitivity, 1.5) * 0.05f + 0.1f);
        float pow = sens * sens * sens * 1.2F;
        yaw -= yaw % pow;
        pitch -= pitch % (pow * sens);
        return new Vector2f(yaw, pitch);
    }


    public void look(Event event, Vector2f rotation, Correction correction, LivingEntity livingEntity) {
        if (event instanceof EventTrace eventTrace) {
            eventTrace.setYaw(rotation.x);
            eventTrace.setPitch(rotation.y);
            eventTrace.setCancelled(true);
        }

        if (event instanceof EventMotion eventMotion) {
            eventMotion.setYaw(rotation.x);
            eventMotion.setPitch(rotation.y);
            mc.player.rotationYawHead = rotation.x;
            mc.player.renderYawOffset = rotation.x;
            mc.player.rotationPitchHead = rotation.y;
        }

        if (correction.equals(Correction.CLIENT)) {
            mc.player.rotationYaw = rotation.x;
            mc.player.rotationPitch = rotation.y;
            return;
        }
        if (correction.equals(Correction.NONE)) return;
        if (event instanceof EventMoveFix eventMoveFix) {
            eventMoveFix.setYaw(rotation.x);
            eventMoveFix.setPitch(rotation.y);
        }

        if (event instanceof EventInputMove e && !correction.equals(Correction.STRICT)) {
            if (correction.equals(Correction.SILENT)) {
                e.setYaw(rotation.x);
            } else if (correction.equals(Correction.FULL) && livingEntity != null) {
                e.setYaw(rotation.x, get(livingEntity.getPositionVec()).x);
            }
        }

        if (event instanceof EventJump eventJump) {
            eventJump.setYaw(rotation.x);
        }
    }

    public enum Correction {
        NONE,
        SILENT,
        STRICT,
        FULL,
        CLIENT
    }
}
