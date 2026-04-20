/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.etc1337.api.baritone.api.event.events;

import lombok.Getter;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import ru.etc1337.api.baritone.api.event.events.type.EventState;

/**
 * @author Brady
 * @since 8/6/2018
 */
@Getter
public final class BaritonePacketEvent {

    private final NetworkManager networkManager;

    private final EventState state;

    private final IPacket<?> packet;

    public BaritonePacketEvent(NetworkManager networkManager, EventState state, IPacket<?> packet) {
        this.networkManager = networkManager;
        this.state = state;
        this.packet = packet;
    }

}
