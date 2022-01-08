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

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import io.vram.modkeys.api.ModKey;

public class ModKeyImpl implements ModKey {
	protected final ResourceLocation name;
	public final int index;

	protected ModKeyImpl(ResourceLocation name, int index) {
		this.name = name;
		this.index = index;
	}

	@Override
	public boolean isPressed(Player player) {
		return ((PlayerModKeyAccess) player).isPressed(index);
	}

	@Override
	public ResourceLocation name() {
		return name;
	}

	private static ModKeyImpl create(ResourceLocation name, int index) {
		final var result = new ModKeyImpl(name, index);
		MAP.put(name, result);
		LIST.add(result);
		return result;
	}

	public static synchronized ModKey getOrCreate(ResourceLocation name) {
		return MAP.computeIfAbsent(name, ((ResourceLocation n) -> create(n, nextIndex++)));
	}

	public static FriendlyByteBuf createJoinPacket() {
		final FriendlyByteBuf result = new FriendlyByteBuf(Unpooled.buffer());
		result.writeVarInt(LIST.size());

		for (final var k : LIST) {
			result.writeResourceLocation(k.name);
		}

		return result;
	}

	public static synchronized @Nullable ModKeyImpl getIfExists(ResourceLocation name) {
		return MAP.get(name);
	}

	public static final int SHIFT_INDEX = 0;
	public static final int CTL_INDEX = 1;
	public static final int ALT_INDEX = 2;
	public static final int MENU_INDEX = 3;
	private static final int FIRST_EXTERNAL_INDEX = MENU_INDEX + 1;

	protected static int nextIndex = FIRST_EXTERNAL_INDEX;
	protected static final Object2ObjectOpenHashMap<ResourceLocation, ModKeyImpl> MAP = new Object2ObjectOpenHashMap<>();
	protected static final ObjectArrayList<ModKeyImpl> LIST = new ObjectArrayList<>();

	static {
		// Ensure physical keys are always present with the same indexes
		create(ModKey.SHIFT, SHIFT_INDEX);
		create(ModKey.CTL, CTL_INDEX);
		create(ModKey.ALT, ALT_INDEX);
		create(ModKey.MENU, MENU_INDEX);
	}
}
