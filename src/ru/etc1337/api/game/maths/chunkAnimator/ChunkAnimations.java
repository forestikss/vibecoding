package ru.etc1337.api.game.maths.chunkAnimator;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import ru.etc1337.Client;
import ru.etc1337.api.game.maths.chunkAnimator.impl.*;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.client.modules.impl.render.ChunkAnimator;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.WeakHashMap;

public class ChunkAnimations implements QuickImports {

    public int mode;

    private final Minecraft mc = Minecraft.getInstance();
    private final WeakHashMap<ChunkRenderDispatcher.ChunkRender, AnimationData> timeStamps = new WeakHashMap<>();

    public void preRender(ChunkRenderDispatcher.ChunkRender chunkRender, @Nullable final MatrixStack matrixStack, double x, double y, double z) {
        ChunkAnimator chunkAnimator = Client.getInstance().getModuleManager().get(ChunkAnimator.class);

        if(!chunkAnimator.isEnabled()) return;

        final AnimationData animationData = timeStamps.get(chunkRender);

        if (animationData == null)
            return;

        int animationDuration = (int) chunkAnimator.time.getValue();

        this.mode = chunkAnimator.modes.getIndex();

        long time = animationData.timeStamp;
        if (time == -1L) {
            time = System.currentTimeMillis();
            animationData.timeStamp = time;
            if (mode == 4) {
                animationData.chunkFacing = mc.player != null ? this.getChunkFacing(this.getZeroedPlayerPos(mc.player).subtract(this.getZeroedCenteredChunkPos(chunkRender.getPosition()))) : Direction.NORTH;
            }
        }
        final long timeDif = System.currentTimeMillis() - time;
        if (timeDif < animationDuration) {
            final int chunkY = chunkRender.getPosition().getY();
            final int animationMode = mode == 2 ? (chunkY < Objects.requireNonNull(this.mc.world).getWorldInfo().getVoidFogHeight() ? 0 : 1) : mode == 4 ? 3 : mode;

            switch (animationMode) {
                case 0:
                    this.translate(matrixStack, 0, -chunkY + this.getFunctionValue(timeDif, 0, chunkY, animationDuration), 0);
                    break;
                case 1:
                    this.translate(matrixStack, 0, 256 - chunkY - this.getFunctionValue(timeDif, 0, 256 - chunkY, animationDuration), 0);
                    break;
                case 3:
                    Direction chunkFacing = animationData.chunkFacing;
                    if (chunkFacing != null) {
                        final Vector3i vec = chunkFacing.getDirectionVec();
                        final double mod = -(200 - this.getFunctionValue(timeDif, 0, 200, animationDuration));

                        this.translate(matrixStack, vec.getX() * mod, 0, vec.getZ() * mod);
                    }
                    break;
            }
        } else {
            this.timeStamps.remove(chunkRender);
        }
    }

    /**
     * Translates with correct method, depending on whether OptiFine is installed ({@link MatrixStack}
     * not used so set to null), or not.
     *
     * @param matrixStack The {@link MatrixStack} object, or null if OptiFine is loaded.
     * @param x The x to translate by.
     * @param y The y to translate by.
     * @param z The z to translate by.
     */
    @SuppressWarnings("deprecation")
    private void translate (@Nullable final MatrixStack matrixStack, final double x, final double y, final double z) {
        if (matrixStack == null) {
            GlStateManager.translated(x, y, z); // OptiFine still uses GlStateManager.
        } else {
            matrixStack.translate(x, y, z);
        }
    }

    private float getFunctionValue(final float t, @SuppressWarnings("SameParameterValue") final float b, final float c, final float d) {
        switch (mode) {
            case 0:
                return Linear.easeOut(t, b, c, d);
            case 1:
                return Quad.easeOut(t, b, c, d);
            case 2:
                return Cubic.easeOut(t, b, c, d);
            case 3:
                return Quart.easeOut(t, b, c, d);
            case 4:
                return Quint.easeOut(t, b, c, d);
        }

        return Linear.easeOut(t, b, c, d);
    }

    public void setPosition(final ChunkRenderDispatcher.ChunkRender renderChunk, final BlockPos position) {
        if (mc.player == null)
            return;

        final BlockPos zeroedPlayerPos = this.getZeroedPlayerPos(mc.player);
        final BlockPos zeroedCenteredChunkPos = this.getZeroedCenteredChunkPos(position);

        if (zeroedPlayerPos.distanceSq(zeroedCenteredChunkPos) > (32 * 32)) {
            timeStamps.put(renderChunk, new AnimationData(-1L,mode == 3 ?
                    this.getChunkFacing(zeroedPlayerPos.subtract(zeroedCenteredChunkPos)) : null));
        } else {
            timeStamps.remove(renderChunk);
        }
    }

    private BlockPos getZeroedPlayerPos (final ClientPlayerEntity player) {
        final BlockPos playerPos = player.getPosition();
        return playerPos.add(0, -playerPos.getY(), 0);
    }

    private BlockPos getZeroedCenteredChunkPos(final BlockPos position) {
        return position.add(8, -position.getY(), 8);
    }

    private Direction getChunkFacing(final Vector3i dif) {
        int difX = Math.abs(dif.getX());
        int difZ = Math.abs(dif.getZ());

        return difX > difZ ? dif.getX() > 0 ? Direction.EAST : Direction.WEST : dif.getZ() > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    public void clear () {
        this.timeStamps.clear();
    }

    private static class AnimationData {
        public long timeStamp;
        public Direction chunkFacing;

        public AnimationData(final long timeStamp, final Direction chunkFacing) {
            this.timeStamp = timeStamp;
            this.chunkFacing = chunkFacing;
        }
    }

}