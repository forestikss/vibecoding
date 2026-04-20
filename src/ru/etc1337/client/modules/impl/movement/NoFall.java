package ru.etc1337.client.modules.impl.movement;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import lombok.Getter;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.viamcp.ViaLoadingBase;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.kotopushka.compiler.sdk.annotations.Compile;

@Getter
@ModuleInfo(name = "No Fall", description = "Позволяет не получать урон от падения", category = ModuleCategory.MOVEMENT)
public class NoFall extends Module {

    public final ModeSetting mode = new ModeSetting("Режим", this, "Grim 1.17+");

    @Compile
    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (mode.is("Grim 1.17+") && ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_17)) {
                if (mc.player.fallDistance > 3.0F) {
                    Vector3d pos = mc.player.getPositionVec();
                    mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(pos.x, pos.y + 1e-7, pos.z, mc.player.rotationYaw, mc.player.rotationPitch, false));
                    mc.player.fallDistance = 0;
                }
            }
        }
    }
}