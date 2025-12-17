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

package uk.co.notnull.ProxyChat.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.typesafe.config.Config;
import uk.co.notnull.ProxyChat.account.ProxyChatAccountManager;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.enums.ChannelType;
import uk.co.notnull.ProxyChat.api.utils.ChatUtils;
import uk.co.notnull.ProxyChat.message.Messages;
import uk.co.notnull.ProxyChat.message.MessagesService;
import uk.co.notnull.ProxyChat.module.ProxyChatModuleManager;
import uk.co.notnull.ProxyChat.api.permission.Permission;
import uk.co.notnull.ProxyChat.permission.PermissionManager;

public class StaffChatListener {
  private final boolean passToBackendServer =
      ProxyChatModuleManager.STAFF_CHAT_MODULE
          .getModuleSection()
          .getBoolean("passToBackendServer");
  private final Config symbolSection =
      ProxyChatModuleManager.STAFF_CHAT_MODULE.getModuleSection().getConfig("symbol");

  @Subscribe
  public void onPlayerChat(PlayerChatEvent e) {
    if (!e.getResult().isAllowed()) return;
    if (e.getPlayer() == null) return;

    Player sender = e.getPlayer();
    String message = e.getMessage();
    ProxyChatAccount account = ProxyChatAccountManager.getAccount(sender).orElseThrow();

    if (ChatUtils.isCommand(message)) return;

    if (symbolSection.getBoolean("enabled")) {
      String symbol = symbolSection.getString("symbol");

      if (message.startsWith(symbol) && !symbol.equals("/")) {
        if (!(PermissionManager.hasPermission(sender, Permission.COMMAND_STAFFCHAT))) {
          e.setResult(PlayerChatEvent.ChatResult.denied());
          return;
        }

        if (message.equals(symbol)) {
          MessagesService.sendMessage(sender, Messages.MESSAGE_BLANK.get());
          e.setResult(PlayerChatEvent.ChatResult.denied());
          return;
        }

        e.setResult(passToBackendServer ? PlayerChatEvent.ChatResult.allowed() : PlayerChatEvent.ChatResult.denied());
        MessagesService.sendStaffMessage(sender, message.substring(1));
      }
    }

    if (account.getChannelType() == ChannelType.STAFF) {
      e.setResult(passToBackendServer ? PlayerChatEvent.ChatResult.allowed() : PlayerChatEvent.ChatResult.denied());
      MessagesService.sendStaffMessage(sender, message);
    }
  }
}
