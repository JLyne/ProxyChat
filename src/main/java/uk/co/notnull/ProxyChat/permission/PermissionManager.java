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

package uk.co.notnull.ProxyChat.permission;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import uk.co.notnull.ProxyChat.account.ProxyChatAccountManager;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.permission.Permission;
import uk.co.notnull.ProxyChat.message.Messages;
import uk.co.notnull.ProxyChat.message.MessagesService;

public final class PermissionManager {
  public static boolean hasPermissionNoMessage(Player player, Permission permission) {
    return player.hasPermission(permission.getStringedPermission());
  }

  public static boolean hasPermissionNoMessage(CommandSource sender, Permission permission) {
    return !(sender instanceof Player)
    || hasPermissionNoMessage((Player) sender, permission);
  }

  public static boolean hasPermissionNoMessage(ProxyChatAccount account, Permission permission) {
    return hasPermissionNoMessage(
			ProxyChatAccountManager.getCommandSource(account).orElseThrow(), permission);
  }

  public static boolean hasPermission(Player player, Permission permission) {
    if (hasPermissionNoMessage(player, permission)) return true;
    else {
      if (permission.isWarnOnLackingPermission()) {
        MessagesService.sendMessage(player, Messages.NO_PERMISSION.get(player));
      }

      return false;
    }
  }

  public static boolean hasPermission(CommandSource sender, Permission permission) {
    return !(sender instanceof Player) || hasPermission((Player) sender, permission);
  }

	private PermissionManager() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
}
