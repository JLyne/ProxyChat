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
import uk.co.notnull.ProxyChat.api.account.AccountManager;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.enums.ChannelType;
import uk.co.notnull.ProxyChat.api.module.ModuleManager;
import uk.co.notnull.ProxyChat.message.Messages;
import uk.co.notnull.ProxyChat.message.MessagesService;
import uk.co.notnull.ProxyChat.module.ProxyChatModuleManager;
import uk.co.notnull.ProxyChat.api.permission.Permission;
import uk.co.notnull.ProxyChat.permission.PermissionManager;

public class ChannelTypeCorrectorListener {
  @Subscribe(priority = Short.MAX_VALUE - 1)
  public void onPlayerChat(PlayerChatEvent e) {
    if (!e.getResult().isAllowed()) return;
    if (e.getPlayer() == null) return;
    if (!ProxyChatModuleManager.GLOBAL_CHAT_MODULE.isEnabled()
        && !ProxyChatModuleManager.LOCAL_CHAT_MODULE.isEnabled()) return;

    Player sender = e.getPlayer();
    ProxyChatAccount player = AccountManager.getAccount(sender.getUniqueId()).orElseThrow();
    ChannelType channel = player.getChannelType();

    if (((channel == ChannelType.GLOBAL)
            && (!ModuleManager.isModuleActive(ProxyChatModuleManager.GLOBAL_CHAT_MODULE)
                || !PermissionManager.hasPermission(sender, Permission.COMMAND_GLOBAL)))
        || ((channel == ChannelType.LOCAL)
            && (!ModuleManager.isModuleActive(ProxyChatModuleManager.LOCAL_CHAT_MODULE)
                || !PermissionManager.hasPermission(sender, Permission.COMMAND_LOCAL)))
        || ((channel == ChannelType.STAFF)
            && (!ModuleManager.isModuleActive(ProxyChatModuleManager.STAFF_CHAT_MODULE)
                || !PermissionManager.hasPermission(sender, Permission.COMMAND_STAFFCHAT)))) {

      e.setResult(PlayerChatEvent.ChatResult.denied());
      ChannelType defaultChannel = player.getDefaultChannelType();

      if (((defaultChannel == ChannelType.GLOBAL)
              && PermissionManager.hasPermissionNoMessage(sender, Permission.COMMAND_GLOBAL))
          || ((defaultChannel == ChannelType.LOCAL)
              && PermissionManager.hasPermissionNoMessage(sender, Permission.COMMAND_LOCAL))) {
        player.setChannelType(defaultChannel);
        MessagesService.sendMessage(sender, Messages.BACK_TO_DEFAULT.get());
      }
    }
  }
}
