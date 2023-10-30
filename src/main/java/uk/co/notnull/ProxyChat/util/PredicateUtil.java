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

import com.typesafe.config.Config;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.permission.Permission;
import uk.co.notnull.ProxyChat.module.ProxyChatModuleManager;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PredicateUtil {
	public static Predicate<ProxyChatAccount> getServerListPredicate(Config section) {
		if (!section.getBoolean("enabled")) return account -> true;
		else {
			// TODO: Use wildcard string
			List<String> allowedServers = section.getStringList("list");

			return account -> allowedServers.contains(account.getServerName());
		}
	}

	public static Predicate<ProxyChatAccount> getGlobalPredicate() {
		return ProxyChatModuleManager.GLOBAL_CHAT_MODULE.getGlobalPredicate();
	}

	public static Predicate<ProxyChatAccount> getNotIgnoredPredicate(ProxyChatAccount sender) {
		return ProxyChatModuleManager.IGNORING_MODULE.getNotIgnoredPredicate(sender);
	}

	public static Predicate<ProxyChatAccount> getPermissionPredicate(Permission permission) {
		return account -> account.hasPermission(permission);
	}

	public static Predicate<ProxyChatAccount> getServerPredicate(RegisteredServer server) {
		return account -> server.equals(account.getServer().orElse(null));
	}

	public static Predicate<ProxyChatAccount> getLocalChatEnabledPredicate() {
		final Config serverList =
				ProxyChatModuleManager.LOCAL_CHAT_MODULE.getModuleSection().getConfig("serverList");
		final Config passThruServerList =
				ProxyChatModuleManager.LOCAL_CHAT_MODULE
						.getModuleSection()
						.getConfig("passThruServerList");

		return Stream.of(serverList, passThruServerList)
				.flatMap(PredicateUtil::serverListToPredicate)
				.reduce(Predicate::or).orElse(account -> true);
	}

	public static Predicate<ProxyChatAccount> getMulticastPredicate(RegisteredServer source) {
		List<List<String>> multiCastServerGroups = ProxyChatModuleManager.MULTICAST_CHAT_MODULE.getMultiCastServerGroups();

		if (multiCastServerGroups == null) {
			return account -> false;
		} else {
			return account -> {
				final RegisteredServer server = account.getServer().orElse(null);

				if(server == null || server.equals(source)) {
					return false;
				}

				final String accountServerName = server.getServerInfo().getName();

				for (List<String> group : multiCastServerGroups) {
					if (group.contains(accountServerName)) {
						return group.contains(source.getServerInfo().getName());
					}
				}

				return false;
			};
		}
	}

	public static Predicate<ProxyChatAccount> getInclusiveMulticastPredicate(RegisteredServer server) {
		return getServerPredicate(server).or(getMulticastPredicate(server));
	}

	private static Stream<Predicate<ProxyChatAccount>> serverListToPredicate(Config section) {
		if (section.getBoolean("enabled")) {
			// TODO: Use wildcard string
			List<String> allowedServers = section.getStringList("list");

			return Stream.of(account -> allowedServers.contains(account.getServerName()));
		} else {
			return Stream.empty();
		}
	}
}
