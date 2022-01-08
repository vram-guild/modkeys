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

package io.vram.modkeys.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import io.vram.modkeys.impl.ModKeyImpl;

public interface ModKey {
	ResourceLocation name();

	boolean isPressed(Player player);

	static ModKey getOrCreate(ResourceLocation name) {
		return ModKeyImpl.getOrCreate(name);
	}

	ResourceLocation SHIFT = new ResourceLocation("xb:shift");
	ResourceLocation CTL = new ResourceLocation("xb:ctl");
	ResourceLocation ALT = new ResourceLocation("xb:alt");
	ResourceLocation MENU = new ResourceLocation("xb:menu");
}
