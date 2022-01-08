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

package io.vram.modkeys.impl.client;

import java.util.Arrays;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import io.vram.modkeys.impl.ModKeys;
import io.vram.modkeys.impl.PlayerModKeyAccess;
import io.vram.modkeys.impl.client.ModKeyBindingImpl.MappedBinding;

public class ModKeysClient {
	private static record KeyMap (MappedBinding binding, int wordIndex, long mask) { }
	private static final ObjectArrayList<KeyMap> KEYS = new ObjectArrayList<>();
	private static long[] bits = new long[0];

	public static void intialize() {
		NetworkManager.registerReceiver(NetworkManager.s2c(), ModKeys.JOIN_PACKET_ID, ModKeysClient::receiveJoinPacketFromServer);
		ClientTickEvent.CLIENT_PRE.register(ModKeysClient::tick);
	}

	private static void receiveJoinPacketFromServer(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
		final int nameCount = buf.readVarInt();
		final ResourceLocation[] names = new ResourceLocation[nameCount];

		for (int i = 0; i < nameCount; ++i) {
			names[i] = buf.readResourceLocation();
		}

		context.queue(() -> clearAndLoadFromServer(names));
	}

	private static void clearAndLoadFromServer(ResourceLocation[] names) {
		final int nameCount = names.length;
		bits = new long[(nameCount + 63) / 64];
		KEYS.clear();

		for (int i = 0; i < nameCount; ++i) {
			final var k = ModKeyBindingImpl.getIfExists(names[i]);

			if (k != null) {
				KEYS.add(new KeyMap(k, i >> 6, 1L << (i & 63)));
			}
		}
	}

	private static void tick(Minecraft mc) {
		ModKeyBindingImpl.updateFixedKeys(mc);
		final long[] newState = computeKeyState();

		if (!Arrays.equals(newState, bits)) {
			bits = newState;
			NetworkManager.sendToServer(ModKeys.UPDATE_PACKET_ID, createUpdatePacket());

			// Update local player also in case any mods test client-side state
			if (mc.player != null) {
				((PlayerModKeyAccess) mc.player).arch_modKeyBits(newState);
			}
		}
	}

	private static long[] computeKeyState() {
		final long[] result = new long[bits.length];

		for (final var k : KEYS) {
			if (k.binding.isPressed()) {
				result[k.wordIndex] |= k.mask;
			}
		}

		return result;
	}

	private static FriendlyByteBuf createUpdatePacket() {
		return new FriendlyByteBuf(Unpooled.buffer()).writeLongArray(bits);
	}
}
