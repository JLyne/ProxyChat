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
import uk.co.notnull.ProxyChat.module.ClearChatModule;
import uk.co.notnull.ProxyChat.api.permission.Permission;
import uk.co.notnull.ProxyChat.permission.PermissionManager;
import uk.co.notnull.ProxyChat.util.PredicateUtil;
import uk.co.notnull.ProxyChat.util.ServerNameUtil;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ClearChatCommand extends BaseCommand {
  // package because only neighboring class needs it
  static final List<String> arg1Completetions = Arrays.asList("local", "global");

  private static final String USAGE = "/clearchat <local [server]|global>";
  private static final Component EMPTY_LINE = Component.newline();

  public ClearChatCommand(ClearChatModule clearChatModule) {
    super(
			"clearchat",
			Permission.COMMAND_CLEAR_CHAT,
			clearChatModule.getModuleSection().getStringList("aliases"));
  }

  @Override
  public void execute(Invocation invocation) {
    if (!PermissionManager.hasPermission(invocation.source(), Permission.COMMAND_CLEAR_CHAT)) return;

    if ((invocation.arguments().length < 1) || (invocation.arguments().length > 3)) {
      MessagesService.sendMessage(invocation.source(), Messages.INCORRECT_USAGE.get(invocation.source(), USAGE));
    } else {

      final int lines =
          ProxyChatModuleManager.CLEAR_CHAT_MODULE.getModuleSection().getInt("emptyLines");
      final ProxyChatAccount proxyChatAccount = ProxyChatAccountManager.getAccount(invocation.source()).orElseThrow();

      if (invocation.arguments()[0].equalsIgnoreCase("local")) {
        boolean serverSpecified = invocation.arguments().length == 2;

          if (!serverSpecified && !(invocation.source() instanceof Player)) {
            MessagesService.sendMessage(
                invocation.source(), Messages.INCORRECT_USAGE.get(proxyChatAccount, USAGE));
            return;
          }

        Optional<RegisteredServer> server = serverSpecified ?
              ServerNameUtil.verifyServerName(invocation.arguments()[1], invocation.source()) :
              proxyChatAccount.getServer();

        if (server.isEmpty()) return;

        clearLocalChat(server.get(), lines);

        MessagesService.sendToMatchingPlayers(
                Messages.CLEARED_LOCAL.get(invocation.source()), PredicateUtil.getLocalPredicate(server.get()));
      } else if (invocation.arguments()[0].equalsIgnoreCase("global")) {
        clearGlobalChat(lines);

        MessagesService.sendToMatchingPlayers(
            Messages.CLEARED_GLOBAL.get(invocation.source()), PredicateUtil.getGlobalPredicate());
      } else {
        MessagesService.sendMessage(invocation.source(), Messages.INCORRECT_USAGE.get(invocation.source(), USAGE));
      }
    }
  }

  @Override
  public List<String> suggest(Invocation invocation) {
    if (invocation.arguments().length == 0) {
      return arg1Completetions;
    }

    final String location = invocation.arguments()[0];

    if (invocation.arguments().length == 1) {
      return arg1Completetions.stream()
          .filter(completion -> completion.startsWith(location))
          .collect(Collectors.toList());
    } else if ((invocation.arguments().length == 2) && ("local".equals(location))) {
      final String serverName = invocation.arguments()[1];

      return ServerNameUtil.getMatchingServerNames(serverName);
    }

    return super.suggest(invocation);
  }

  public static void clearGlobalChat(int emptyLines) {
    clearChat(emptyLines, PredicateUtil.getGlobalPredicate());
  }

  public static void clearLocalChat(RegisteredServer server, int emptyLines) {
    clearChat(emptyLines, PredicateUtil.getLocalPredicate(server));
  }

  private static void clearChat(int emptyLines, Predicate<ProxyChatAccount> predicate) {
    for (int i = 0; i < emptyLines; i++) {
      MessagesService.sendToMatchingPlayers(EMPTY_LINE, predicate);
    }
  }
}
