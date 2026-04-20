package ru.etc1337.client.modules.impl.combat;


import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventAttack;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.viamcp.ViaLoadingBase;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@ModuleInfo(name = "Criticals", description = "Позволяет бить критами с места", category = ModuleCategory.COMBAT)
public class Criticals extends Module {
    public final ModeSetting modeSetting = new ModeSetting("Режим", this, "Grim 1.17+");

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventAttack)) return;
        if (!canCrit()) return;

        Vector3d pos = mc.player.getPositionVec();
        if (modeSetting.is("Grim 1.17+")) {
            mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y - 1e-6, pos.z, mc.player.rotationYaw, mc.player.rotationPitch, false));
        }
    }

    public static boolean canCrit() {
        return ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_17) && !mc.player.isOnGround();
    }

}
