/*
 * This file is part of ViaMCP - https://github.com/FlorianMichael/ViaMCP
 * Copyright (C) 2020-2023 FlorianMichael/EnZaXD and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.etc1337.api.viamcp;

import com.viaversion.viabackwards.protocol.protocol1_16_4to1_17.Protocol1_16_4To1_17;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.ServerboundPackets1_16_2;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.ClientboundPackets1_17;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.ServerboundPackets1_17;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import ru.etc1337.api.config.Directory;
import ru.etc1337.api.viamcp.protocolinfo.ProtocolInfo;

import java.io.File;
import java.util.List;

public class ViaMCP {
    public final static int NATIVE_VERSION = 754; // 1.16.4/5
    public static ViaMCP INSTANCE;
    @Getter
    private final VersionSelectScreen viaScreen;

    public static void create() {
        INSTANCE = new ViaMCP();
    }

    public ViaMCP() {
        ViaLoadingBase.ViaLoadingBaseBuilder.create().runDirectory(new File(Minecraft.getInstance().gameDir, "ViaMCP")).nativeVersion(NATIVE_VERSION).build();
        this.viaScreen = new VersionSelectScreen(Minecraft.getInstance().fontRenderer, 5, 5, 100, 20, ITextComponent.getTextComponentOrEmpty(ProtocolVersion.getProtocol(NATIVE_VERSION).getName()));

        fixTransactions();
    }

    private void fixTransactions() {
        final Protocol1_16_4To1_17 protocol = Via.getManager().getProtocolManager().getProtocol(Protocol1_16_4To1_17.class);
        protocol.registerClientbound(ClientboundPackets1_17.PING, ClientboundPackets1_16_2.WINDOW_CONFIRMATION, wrapper -> {}, true);
        protocol.registerServerbound(ServerboundPackets1_16_2.WINDOW_CONFIRMATION, ServerboundPackets1_17.PONG, wrapper -> {}, true);
    }

    public List<ProtocolInfo> getAllProtocols() {
        return ProtocolInfo.PROTOCOL_INFOS;
    }

}