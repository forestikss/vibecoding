package ru.etc1337.api.viamcp.fixes;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;

import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.viamcp.ViaLoadingBase;

public class AttackOrder implements QuickImports {

    public static void sendConditionalSwing(RayTraceResult ray, Hand enumHand) {
        if (ray != null && ray.getType() != RayTraceResult.Type.ENTITY) {
            AttackOrder.mc.player.swingArm(enumHand);
        }
    }

    public static void sendFixedAttack(PlayerEntity entityIn, Entity target, Hand enumHand) {
        if (ViaLoadingBase.getInstance().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            AttackOrder.mc.player.swingArm(enumHand);
            AttackOrder.mc.playerController.attackEntity(entityIn, target);
        } else {
            AttackOrder.mc.playerController.attackEntity(entityIn, target);
            AttackOrder.mc.player.swingArm(enumHand);
        }
    }
}