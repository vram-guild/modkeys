/*
 * This file is part of Modifier Keys and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.vram.modkeys.impl;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ModKeys {
	public static final String MODID = "modkeys";
	public static final Logger LOG = LogManager.getLogger("modkeys");

	public static final ResourceLocation UPDATE_PACKET_ID = new ResourceLocation("architectury", "modkeys_update");
	public static final ResourceLocation JOIN_PACKET_ID = new ResourceLocation("architectury", "modkeys_join");

	public static void initialize() {
		NetworkManager.registerReceiver(NetworkManager.c2s(), UPDATE_PACKET_ID, ModKeys::receiveUpdateFromClient);

		PlayerEvent.PLAYER_JOIN.register(p -> {
			NetworkManager.sendToPlayer(p, JOIN_PACKET_ID, ModKeyImpl.createJoinPacket());
		});
	}

	private static void receiveUpdateFromClient(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
		final var bits = buf.readLongArray();
		final var player = context.getPlayer();

		context.queue(() -> {
			if (player != null) {
				((PlayerModKeyAccess) player).arch_modKeyBits(bits);
			}
		});
	}
}
