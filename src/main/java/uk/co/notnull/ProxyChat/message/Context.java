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

package uk.co.notnull.ProxyChat.message;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import uk.co.notnull.ProxyChat.account.ProxyChatAccountManager;
import uk.co.notnull.ProxyChat.api.placeholder.ProxyChatContext;

public class Context extends ProxyChatContext {
  public Context(CommandSource sender) {
    super(ProxyChatAccountManager.getAccount(sender).get());
  }

  public Context(CommandSource sender, String message) {
    this(sender);

    setMessage(message);

    if(sender instanceof Player player) {
      player.getCurrentServer().map(ServerConnection::getServer).ifPresent(this::setServer);
    }
  }

  public Context(CommandSource player, String message, RegisteredServer server) {
    this(player, message);

    setServer(server);
  }

  public Context(CommandSource sender, CommandSource target) {
    super(
			ProxyChatAccountManager.getAccount(sender).get(),
			ProxyChatAccountManager.getAccount(target).get());
  }

  public Context(CommandSource sender, CommandSource target, String message) {
    this(sender, target);

    setMessage(message);
  }
}
