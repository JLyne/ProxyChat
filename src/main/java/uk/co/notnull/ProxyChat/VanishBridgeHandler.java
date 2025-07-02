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

package uk.co.notnull.ProxyChat;

import com.velocitypowered.api.event.Subscribe;
import uk.co.notnull.ProxyChat.api.account.AccountManager;
import uk.co.notnull.ProxyChat.api.event.ProxyChatJoinEvent;
import uk.co.notnull.vanishbridge.api.VanishBridgeAPI;
import uk.co.notnull.vanishbridge.api.VanishStateChangeEvent;

public class VanishBridgeHandler {
	private final ProxyChat plugin;
	private final VanishBridgeAPI vnishBridgeAPI;

	public VanishBridgeHandler(ProxyChat plugin) {
		this.plugin = plugin;
		this.vnishBridgeAPI = (VanishBridgeAPI) plugin.getProxy().getPluginManager()
				.getPlugin("vanishbridge").orElseThrow().getInstance().orElseThrow();


		plugin.getProxy().getEventManager().register(plugin, this);
	}

	@Subscribe
	public void onVanishStateChange(VanishStateChangeEvent event) {
		AccountManager.getAccount(event.getPlayer().getUniqueId())
				.ifPresent(account -> account.setVanished(event.isVanishing()));
	}

	@Subscribe
	public void onVanishStateChange(ProxyChatJoinEvent event) {
		AccountManager.getAccount(event.getPlayer().getUniqueId())
				.ifPresent(account -> account.setVanished(vnishBridgeAPI.isVanished(event.getPlayer())));
	}
}
