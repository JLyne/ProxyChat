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
import uk.co.notnull.ProxyChat.command.LocalChatCommand;
import uk.co.notnull.ProxyChat.listener.LocalChatListener;

public class LocalChatModule extends Module {
  private LocalChatCommand localChatCommand;
  private LocalChatListener localChatListener;

  @Override
  public String getName() {
    return "LocalChat";
  }

  @Override
  public void onEnable() {
    localChatCommand = new LocalChatCommand(this);
    localChatListener = new LocalChatListener();

    localChatCommand.register();
    ProxyChat.getInstance().getProxy()
        .getEventManager()
        .register(ProxyChat.getInstance(), localChatListener);
  }

  @Override
  public void onDisable() {
    localChatCommand.unregister();
    ProxyChat.getInstance().getProxy().getEventManager().unregisterListener(ProxyChat.getInstance(), localChatListener);
  }
}