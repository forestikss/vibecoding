package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.PacketInterface;
import de.maxhenkel.voicechat.Voicechat;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;
import ru.etc1337.Client;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.handler.EventListener;
import ru.etc1337.api.events.impl.packet.EventReceivePacket;
import ru.etc1337.api.events.impl.packet.EventSendPacket;
import ru.etc1337.client.modules.impl.misc.VoiceChat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FabricNetManager extends de.maxhenkel.voicechat.net.NetManager implements EventListener {

    @Getter
    private final Set<ResourceLocation> packets;
    private final Map<ResourceLocation, PacketInterface> packetInterfaces;

    public FabricNetManager() {
        packets = new HashSet<>();
        packetInterfaces = new HashMap<>();
        Client.getEventManager().register(this);
    }


    @Override
    public void onEvent(Event event) {
        if (event instanceof EventReceivePacket eventReceivePacket) {
            if (!Client.getInstance().getModuleManager().get(VoiceChat.class).isEnabled()) {
                return;
            }
            if (eventReceivePacket.getPacket() instanceof SCustomPayloadPlayPacket customPayloadPacket) {
                ResourceLocation channel = customPayloadPacket.getChannelName();
                PacketInterface handler = packetInterfaces.get(channel);

                if (handler != null) {
                    try {
                        PacketBuffer buffer = new PacketBuffer(customPayloadPacket.getBufferData().copy());
                        handler.execute(Minecraft.getInstance().player, buffer);
                        buffer.release();
                    } catch (Exception e) {
                        Voicechat.LOGGER.error("Failed to handle packet for channel {}", channel, e);
                    }
                }
            }
        }
    }

    @Override
    public <T extends Packet<T>> Channel<T> registerReceiver(Class<T> packetType, boolean toClient, boolean toServer) {
        ClientServerChannel<T> channel = new ClientServerChannel<>();

        try {
            T dummyPacket = packetType.getDeclaredConstructor().newInstance();
            ResourceLocation identifier = dummyPacket.getIdentifier();
            packets.add(identifier);

            if (toClient) {
                packetInterfaces.put(identifier, (player, payload) -> {
                    try {
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(payload);
                        channel.onClientPacket(Minecraft.getInstance(), player.connection, packet);
                    } catch (Exception e) {
                        Voicechat.LOGGER.error("Failed to process packet {}", identifier, e);
                    }
                });
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to register packet", e);
        }

        return channel;
    }
}