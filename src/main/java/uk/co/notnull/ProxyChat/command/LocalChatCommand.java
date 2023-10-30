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

package uk.co.notnull.ProxyChat.command;

import com.velocitypowered.api.proxy.Player;
import uk.co.notnull.ProxyChat.account.ProxyChatAccountManager;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.enums.AccountType;
import uk.co.notnull.ProxyChat.api.enums.ChannelType;
import uk.co.notnull.ProxyChat.message.Messages;
import uk.co.notnull.ProxyChat.message.MessagesService;
import uk.co.notnull.ProxyChat.module.LocalChatModule;
import uk.co.notnull.ProxyChat.module.ProxyChatModuleManager;
import uk.co.notnull.ProxyChat.api.permission.Permission;
import uk.co.notnull.ProxyChat.permission.PermissionManager;
import uk.co.notnull.ProxyChat.util.PredicateUtil;

import java.util.Collections;
import java.util.List;

public class LocalChatCommand extends BaseCommand {
  public LocalChatCommand(LocalChatModule localChatModule) {
    super(
			"local",
			Permission.COMMAND_LOCAL,
			localChatModule.getModuleSection().getStringList("aliases"));
  }

  @Override
  public void execute(Invocation invocation) {
    if (!(invocation.source() instanceof Player)) {
      MessagesService.sendMessage(invocation.source(), Messages.NOT_A_PLAYER.get());
      return;
    }

    if (!PermissionManager.hasPermission(invocation.source(), Permission.COMMAND_LOCAL)) return;

    ProxyChatAccount account = ProxyChatAccountManager.getAccount(invocation.source()).orElseThrow();

    if (!PredicateUtil.getLocalChatEnabledPredicate().test(account)
        && (account.getAccountType() == AccountType.PLAYER)) {
      MessagesService.sendMessage(invocation.source(), Messages.NOT_IN_LOCAL_SERVER.get());
      return;
    }

    if (invocation.arguments().length < 1) {
      if (PermissionManager.hasPermission(invocation.source(), Permission.COMMAND_LOCAL_TOGGLE)) {
        ProxyChatAccount player = ProxyChatAccountManager.getAccount(invocation.source()).orElseThrow();

        if (player.getChannelType() == ChannelType.LOCAL) {
          ChannelType defaultChannelType = player.getDefaultChannelType();
          player.setChannelType(defaultChannelType);

          if (defaultChannelType == ChannelType.LOCAL) {
            MessagesService.sendMessage(invocation.source(), Messages.LOCAL_IS_DEFAULT.get());
          } else {
            MessagesService.sendMessage(invocation.source(), Messages.ENABLE_GLOBAL.get());
          }
        } else {
          player.setChannelType(ChannelType.LOCAL);
          MessagesService.sendMessage(invocation.source(), Messages.ENABLE_LOCAL.get());
        }
      } else {
        MessagesService.sendMessage(
            invocation.source(), Messages.INCORRECT_USAGE.get(invocation.source(), "/local <message>"));
      }
    } else {
      String finalMessage = String.join(" ", invocation.arguments());

      MessagesService.sendLocalMessage(invocation.source(), finalMessage);
    }
  }

  @Override
  public List<String> suggest(Invocation invocation) {
    if(invocation.arguments().length == 0) {
      return Collections.emptyList();
    }

    return ProxyChatModuleManager.EMOJI_MODULE.getEmojiSuggestions(invocation);
  }
}
