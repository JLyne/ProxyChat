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

package uk.co.notnull.ProxyChat.filter;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.filter.BlockMessageException;
import uk.co.notnull.ProxyChat.api.filter.ProxyChatPreParseFilter;
import uk.co.notnull.ProxyChat.api.filter.FilterManager;
import uk.co.notnull.ProxyChat.message.Messages;
import uk.co.notnull.ProxyChat.message.MessagesService;
import uk.co.notnull.ProxyChat.api.permission.Permission;
import uk.co.notnull.ProxyChat.util.PredicateUtil;

import java.util.LinkedList;
import java.util.List;

public class ChatLockFilter implements ProxyChatPreParseFilter {
  private boolean globalLock = false;
  private final List<RegisteredServer> lockedServers = new LinkedList<>();

  @Override
  public String applyFilter(ProxyChatAccount sender, String message) throws BlockMessageException {
    if (sender.hasPermission(Permission.BYPASS_CHAT_LOCK)) {
      return message;
    }

    if(!((globalLock && PredicateUtil.getGlobalPredicate().test(sender)) ||
            (sender.getServer().isPresent() && lockedServers.contains(sender.getServer().get())))) {
      return message;
    }

    throw new ExtendedBlockMessageException(Messages.CHAT_IS_DISABLED, sender);
  }

  @Override
  public int getPriority() {
    return FilterManager.LOCK_CHAT_FILTER_PRIORITY;
  }

  public void enableGlobalChatLock() {
    globalLock = true;
  }

  public void enableLocalChatLock(RegisteredServer name) {
    lockedServers.add(name);
  }

  public boolean isGlobalChatLockEnabled() {
    return globalLock;
  }

  public boolean isLocalChatLockEnabled(RegisteredServer name) {
    return lockedServers.contains(name);
  }

  public void disableGlobalChatLock() {
    globalLock = false;
  }

  public void disableLocalChatLock(RegisteredServer name) {
    lockedServers.remove(name);
  }
}
