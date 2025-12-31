/*
 * ProxyChat, a Velocity chat solution
 * Copyright (C) 2020 James Lyne
 *
 * Based on BungeeChat2 (https://github.com/AuraDevelopmentTeam/BungeeChat2)
 * Copyright (C) 2020 Aura Development Team
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

package uk.co.notnull.ProxyChat.util;

import com.velocitypowered.api.plugin.PluginManager;
import uk.co.notnull.ProxyChat.ProxyChat;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.account.ConsoleAccount;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import uk.co.notnull.platformdetection.Platform;
import uk.co.notnull.platformdetection.PlatformDetectionVelocity;

public final class PlatformUtil {
	private static boolean initialised = false;
	private static PlatformDetectionVelocity platformDetection = null;

	private static void getPlatformAPI() {
		if(initialised) {
			return;
		}

		initialised = true;
		PluginManager pluginManager = ProxyChat.getInstance().getProxy().getPluginManager();

		if(pluginManager.isLoaded("platform-detection")) {
			platformDetection = (PlatformDetectionVelocity) pluginManager.getPlugin("platform-detection").orElseThrow()
					.getInstance().orElseThrow();
		}
	}

	private static Platform getPlatform(ProxyChatAccount player) {
		getPlatformAPI();
		return platformDetection.getPlatform(player.getUniqueId());
	}

	public static String getPlatformIcon(ProxyChatAccount player) {
		getPlatformAPI();
		return platformDetection != null ? getPlatform(player).getIcon() : "";
	}

	public static String getPlatformName(ProxyChatAccount player) {
		getPlatformAPI();

		if(player instanceof ConsoleAccount) {
			return "Velocity";
		}

		return platformDetection != null ? getPlatform(player).getLabel() : "Unknown";
	}

	public static String getPlatformVersion(ProxyChatAccount player) {
		getPlatformAPI();

		if(player instanceof ConsoleAccount) {
			return ProxyChat.getInstance().getProxy().getVersion().getVersion();
		}

		if (platformDetection == null) {
			return "Unknown";
		}

		String version = platformDetection.getPlatformVersion(player.getUniqueId());

		return version != null ? version : "Unknown";
	}

	public static TextComponent getHover(ProxyChatAccount player) {
		TextComponent.Builder result = Component.text().content(player.getDisplayName() + " is using:\n");
		String version = getPlatformVersion(player);

		if(platformDetection == null) {
			result.append().append(Component.text("Unknown\n", NamedTextColor.YELLOW))
				.append(Component.text(version, NamedTextColor.GRAY));

			return result.build();
		}

		Platform platform = getPlatform(player);

		String icon = platform.getIcon();
		String name = platform.getLabel();

		result.append(Component.text(icon))
				.append(Component.text(" " + name + "\n", NamedTextColor.YELLOW))
				.append(Component.text(version, NamedTextColor.GRAY));

		return result.build();
	}

	private PlatformUtil() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
}
