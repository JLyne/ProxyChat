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
import com.velocitypowered.api.proxy.server.RegisteredServer;
import uk.co.notnull.ProxyChat.account.ProxyChatAccountManager;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.message.Messages;
import uk.co.notnull.ProxyChat.message.MessagesService;
import uk.co.notnull.ProxyChat.module.ProxyChatModuleManager;
import uk.co.notnull.ProxyChat.module.ChatLockModule;
import uk.co.notnull.ProxyChat.api.permission.Permission;
import uk.co.notnull.ProxyChat.permission.PermissionManager;
import uk.co.notnull.ProxyChat.util.PredicateUtil;
import uk.co.notnull.ProxyChat.util.ServerNameUtil;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChatLockCommand extends BaseCommand {
  private static final String USAGE = "/chatlock <local [server]|global> [clear]";
  private static final String CLEAR = "clear";

  public ChatLockCommand(ChatLockModule chatLockModule) {
    super(
			"chatlock",
			Permission.COMMAND_CHAT_LOCK,
			chatLockModule.getModuleSection().getStringList("aliases"));
  }

  @Override
  public void execute(Invocation invocation) {
    // The permission check sends the no permission message
    if (!PermissionManager.hasPermission(invocation.source(), Permission.COMMAND_CHAT_LOCK)) return;

    ProxyChatAccount player = ProxyChatAccountManager.getAccount(invocation.source()).orElseThrow();

    if ((invocation.arguments().length < 1) || (invocation.arguments().length > 2)) {
      MessagesService.sendMessage(invocation.source(), Messages.INCORRECT_USAGE.get(player, USAGE));
      return;
    }

    final ChatLockModule chatLock = ProxyChatModuleManager.CHAT_LOCK_MODULE;
    final boolean clear = (invocation.arguments().length >= 2)
            && invocation.arguments()[invocation.arguments().length - 1].equalsIgnoreCase("clear");
    final int emptyLines = clear ? chatLock.getModuleSection().getInt("emptyLinesOnClear") : 0;

    if (invocation.arguments()[0].equalsIgnoreCase("global")) {
      if (chatLock.isGlobalChatLockEnabled()) {
        chatLock.disableGlobalChatLock();
        MessagesService.sendMessage(invocation.source(), Messages.DISABLE_CHATLOCK.get(player));
      } else {
        chatLock.enableGlobalChatLock();

        if (clear) {
          ClearChatCommand.clearGlobalChat(emptyLines);
        }

        MessagesService.sendToMatchingPlayers(
                Messages.ENABLE_CHATLOCK.get(player), PredicateUtil.getGlobalPredicate());
      }
    } else if (invocation.arguments()[0].equalsIgnoreCase("local")) {
      boolean serverSpecified = invocation.arguments().length == (clear ? 3 : 2);

      if (!serverSpecified && !(invocation.source() instanceof Player)) {
        MessagesService.sendMessage(invocation.source(), Messages.INCORRECT_USAGE.get(player, USAGE));
        return;
      }

      Optional<RegisteredServer> server = serverSpecified ?
              ServerNameUtil.verifyServerName(invocation.arguments()[1], invocation.source()) :
              player.getServer();

      if (server.isEmpty()) return;

      if (chatLock.isLocalChatLockEnabled(server.get())) {
        chatLock.disableLocalChatLock(server.get());
        MessagesService.sendMessage(invocation.source(), Messages.DISABLE_CHATLOCK.get(player));
      } else {
        chatLock.enableLocalChatLock(server.get());

        if (clear) {
          ClearChatCommand.clearLocalChat(server.get(), emptyLines);
        }

        MessagesService.sendToMatchingPlayers(
                Messages.ENABLE_CHATLOCK.get(player), PredicateUtil.getInclusiveMulticastPredicate(server.get()));
      }
    } else {
      MessagesService.sendMessage(invocation.source(), Messages.INCORRECT_USAGE.get(player, USAGE));
    }
  }

  @Override
  public List<String> suggest(Invocation invocation) {
    if(invocation.arguments().length == 0) {
      return ClearChatCommand.arg1Completetions;
    }

    final String location = invocation.arguments()[0];

    if (invocation.arguments().length == 1) {
      return ClearChatCommand.arg1Completetions.stream()
          .filter(completion -> completion.startsWith(location))
          .collect(Collectors.toList());
    } else if ((invocation.arguments().length == 2) && ClearChatCommand.arg1Completetions.contains(location)) {
      final String param2 = invocation.arguments()[1];
      final List<String> suggestions = new LinkedList<>();

      if (CLEAR.startsWith(param2)) {
        suggestions.add(CLEAR);
      }

      if ("local".equals(location)) {
        suggestions.addAll(ServerNameUtil.getMatchingServerNames(param2));
      }

      return suggestions;
    } else if ((invocation.arguments().length == 3)
        && "local".equals(location)
        && !CLEAR.equals(invocation.arguments()[1])
        && CLEAR.startsWith(invocation.arguments()[2])) {
      return Collections.singletonList(CLEAR);
    }

    return super.suggest(invocation);
  }
}
