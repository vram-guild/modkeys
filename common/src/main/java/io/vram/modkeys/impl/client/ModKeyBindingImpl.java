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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import io.vram.modkeys.api.ModKey;

public class ModKeyBindingImpl {
	public interface MappedBinding {
		boolean isPressed();

		@Nullable KeyMapping keyMapping();
	}

	private static class FixedBinding implements MappedBinding {
		private boolean isPressed;
		private final int leftKey;
		private final int rightKey;

		private FixedBinding(int leftKey, int rightKey) {
			this.leftKey = leftKey;
			this.rightKey = rightKey;
		}

		private void update(long windowHandle) {
			isPressed = InputConstants.isKeyDown(windowHandle, leftKey) || InputConstants.isKeyDown(windowHandle, rightKey);
		}

		@Override
		public boolean isPressed() {
			return isPressed;
		}

		@Override
		public KeyMapping keyMapping() {
			return null;
		}
	}

	private static class ClientBinding implements MappedBinding {
		final KeyMapping keyMapping;

		private ClientBinding (KeyMapping keyMapping) {
			this.keyMapping = keyMapping;
		}

		@Override
		public boolean isPressed() {
			return keyMapping.isDown();
		}

		@Override
		public KeyMapping keyMapping() {
			return keyMapping;
		}
	}

	private static final Object2ObjectOpenHashMap<ResourceLocation, MappedBinding> CLIENT_BINDINGS = new Object2ObjectOpenHashMap<>();
	private static final ObjectArrayList<FixedBinding> FIXED_BINDINGS = new ObjectArrayList<>();

	private static void addFixedBinding(ResourceLocation name, int leftKey, int rightKey) {
		final var fixedBinding = new FixedBinding(leftKey, rightKey);
		CLIENT_BINDINGS.put(name, fixedBinding);
		FIXED_BINDINGS.add(fixedBinding);
	}

	public static void initialize() {
		addFixedBinding(ModKey.SHIFT, GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT);
		addFixedBinding(ModKey.CTL, GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL);
		addFixedBinding(ModKey.ALT, GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT);
		addFixedBinding(ModKey.MENU, GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER);
	}

	public static void updateFixedKeys(Minecraft mc) {
		final long handle = mc.getWindow().getWindow();

		for (final var fb : FIXED_BINDINGS) {
			fb.update(handle);
		}
	}

	public static MappedBinding getIfExists(ResourceLocation name) {
		return CLIENT_BINDINGS.get(name);
	}

	public static void setBinding(ResourceLocation name, @Nullable KeyMapping keyMapping) {
		if (isMappedToFixedBinding(name)) {
			throw new IllegalArgumentException("Cannot remap fixed modifier keys");
		}

		if (keyMapping == null) {
			CLIENT_BINDINGS.remove(name);
		} else {
			CLIENT_BINDINGS.put(name, new ClientBinding(keyMapping));
		}
	}

	private static boolean isMappedToFixedBinding(ResourceLocation name) {
		final var binding = CLIENT_BINDINGS.get(name);
		return binding != null && binding.keyMapping() == null;
	}

	public static @Nullable KeyMapping getBinding(ResourceLocation name) {
		final var binding = CLIENT_BINDINGS.get(name);
		return binding == null ? null : binding.keyMapping();
	}
}
