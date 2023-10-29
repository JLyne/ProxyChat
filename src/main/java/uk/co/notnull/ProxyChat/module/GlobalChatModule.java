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

package uk.co.notnull.ProxyChat.module;

import uk.co.notnull.ProxyChat.ProxyChat;
import uk.co.notnull.ProxyChat.account.Account;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.enums.ChannelType;
import uk.co.notnull.ProxyChat.command.GlobalChatCommand;
import uk.co.notnull.ProxyChat.listener.GlobalChatListener;
import uk.co.notnull.ProxyChat.util.PredicateUtil;

import java.util.function.Predicate;

public class GlobalChatModule extends Module {
  private GlobalChatCommand globalChatCommand;
  private GlobalChatListener globalChatListener;

  @Override
  public String getName() {
    return "GlobalChat";
  }

  @Override
  public void onEnable() {
    globalChatCommand = new GlobalChatCommand(this);
    globalChatListener = new GlobalChatListener();

    globalChatCommand.register();
    ProxyChat.getInstance().getProxy()
        .getEventManager()
        .register(ProxyChat.getInstance(), globalChatListener);

    if (getModuleSection().getBoolean("default")
        || !ProxyChatModuleManager.LOCAL_CHAT_MODULE.isEnabled()) {
      Account.setDefaultChannelType(ChannelType.GLOBAL);
    }
  }

  @Override
  public void onDisable() {
    globalChatCommand.unregister();
    ProxyChat.getInstance().getProxy()
        .getEventManager()
        .unregisterListener(ProxyChat.getInstance(), globalChatListener);

    Account.setDefaultChannelType(ChannelType.LOCAL);
  }

  public Predicate<ProxyChatAccount> getGlobalPredicate() {
    return PredicateUtil.getServerListPredicate(getModuleSection().getConfig("serverList"));
  }
}
