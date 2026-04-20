package de.maxhenkel.voicechat;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;

public interface PacketInterface {
    void execute(final ClientPlayerEntity localPlayer, final PacketBuffer payload);
}