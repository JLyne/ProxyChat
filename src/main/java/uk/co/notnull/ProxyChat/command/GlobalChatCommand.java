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
import uk.co.notnull.ProxyChat.module.GlobalChatModule;
import uk.co.notnull.ProxyChat.module.ProxyChatModuleManager;
import uk.co.notnull.ProxyChat.api.permission.Permission;
import uk.co.notnull.ProxyChat.permission.PermissionManager;
import uk.co.notnull.ProxyChat.util.PredicateUtil;

import java.util.Collections;
import java.util.List;

public class GlobalChatCommand extends BaseCommand {
  public GlobalChatCommand(GlobalChatModule globalChatModule) {
    super(
			"global",
			Permission.COMMAND_GLOBAL,
			globalChatModule.getModuleSection().getStringList("aliases"));
  }

  @Override
  public void execute(Invocation invocation) {
    if (!PermissionManager.hasPermission(invocation.source(), Permission.COMMAND_GLOBAL)) return;

    ProxyChatAccount account = ProxyChatAccountManager.getAccount(invocation.source()).orElseThrow();

    if (!PredicateUtil.getGlobalPredicate().test(account)
        && (account.getAccountType() == AccountType.PLAYER)) {
      MessagesService.sendMessage(invocation.source(), Messages.NOT_IN_GLOBAL_SERVER.get());

      return;
    }

    if (invocation.arguments().length < 1) {
      if (!(invocation.source() instanceof Player)) {
        MessagesService.sendMessage(invocation.source(), Messages.NOT_A_PLAYER.get());
        return;
      }

      if (PermissionManager.hasPermission(invocation.source(), Permission.COMMAND_GLOBAL_TOGGLE)) {
        ProxyChatAccount player = ProxyChatAccountManager.getAccount(invocation.source()).orElseThrow();

        if (player.getChannelType() == ChannelType.GLOBAL) {
          ChannelType defaultChannelType = player.getDefaultChannelType();
          player.setChannelType(defaultChannelType);

          if (defaultChannelType == ChannelType.LOCAL) {
            MessagesService.sendMessage(invocation.source(), Messages.ENABLE_LOCAL.get());
          } else {
            MessagesService.sendMessage(invocation.source(), Messages.GLOBAL_IS_DEFAULT.get());
          }
        } else {
          player.setChannelType(ChannelType.GLOBAL);
          MessagesService.sendMessage(invocation.source(), Messages.ENABLE_GLOBAL.get());
        }
      } else {
        MessagesService.sendMessage(
            invocation.source(), Messages.INCORRECT_USAGE.get(invocation.source(), "/global <message>"));
      }
    } else {
      String finalMessage = String.join(" ", invocation.arguments());

      MessagesService.sendGlobalMessage(invocation.source(), finalMessage);
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
