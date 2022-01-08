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

public interface PlayerModKeyAccess {
	default boolean isPressed(int index) {
		final long[] bits = arch_modKeyBits();
		if (bits == null) return false;
		final int word = index >> 6;
		return word >= 0 && word < bits.length ? (bits[word] & (1L << (index & 63))) != 0 : false;
	}

	long[] arch_modKeyBits();

	void arch_modKeyBits(long[] bits);
}
