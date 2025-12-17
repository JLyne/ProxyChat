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

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import uk.co.notnull.ProxyChat.account.ProxyChatAccountManager;
import uk.co.notnull.ProxyChat.api.account.AccountManager;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.message.Messages;
import uk.co.notnull.ProxyChat.message.MessagesService;
import uk.co.notnull.ProxyChat.module.IgnoringModule;
import uk.co.notnull.ProxyChat.api.permission.Permission;
import uk.co.notnull.ProxyChat.permission.PermissionManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IgnoreCommand extends BaseCommand {
  private static final List<String> arg1Completetions = Arrays.asList("list", "add", "remove");

  public IgnoreCommand(IgnoringModule ignoringModule) {
    super(
			"ignore",
			Permission.COMMAND_IGNORE,
			ignoringModule.getModuleSection().getStringList("aliases"));
  }

  @Override
  public void execute(Invocation invocation) {
    if (!(invocation.source() instanceof Player)) {
      MessagesService.sendMessage(invocation.source(), Messages.NOT_A_PLAYER.get());
      return;
    }

    if (!PermissionManager.hasPermission(invocation.source(), Permission.COMMAND_IGNORE)) return;

    if (invocation.arguments().length < 1) {
      MessagesService.sendMessage(
          invocation.source(), Messages.INCORRECT_USAGE
                      .get(invocation.source(), "/ignore <list|add|remove> [player]"));
      return;
    }

    ProxyChatAccount player = ProxyChatAccountManager.getAccount(invocation.source()).orElseThrow();

    if (invocation.arguments()[0].equalsIgnoreCase("list")) {
      List<ProxyChatAccount> ignored =
          player.getIgnored().stream()
                  .map(AccountManager::getAccount)
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .toList();

      if (ignored.isEmpty()) {
        MessagesService.sendMessage(invocation.source(), Messages.IGNORE_NOBODY.get(player));
      } else {
        String list =
            ignored.stream()
                .map(ProxyChatAccount::getName)
                .collect(Collectors.joining(", "));

        MessagesService.sendMessage(invocation.source(), Messages.IGNORE_LIST.get(player, list));
      }
    } else if (invocation.arguments()[0].equalsIgnoreCase("add")) {
      if (invocation.arguments().length < 2) {
        MessagesService.sendMessage(
            invocation.source(), Messages.INCORRECT_USAGE.get(invocation.source(), "/ignore add <player>"));
        return;
      }

      Optional<ProxyChatAccount> targetAccount = AccountManager.getAccount(invocation.arguments()[1]);

      if (targetAccount.isEmpty()
          || (targetAccount.get().isVanished()
              && !PermissionManager.hasPermission(invocation.source(), Permission.COMMAND_VANISH_VIEW))) {
        MessagesService.sendMessage(invocation.source(), Messages.PLAYER_NOT_FOUND.get());
        return;
      }

      CommandSource target = ProxyChatAccountManager.getCommandSource(targetAccount.get()).orElseThrow();

      if (target == invocation.source()) {
        MessagesService.sendMessage(invocation.source(), Messages.IGNORE_YOURSELF.get());
        return;
      }

      if (player.hasIgnored(targetAccount.get().getUniqueId())) {
        MessagesService.sendMessage(invocation.source(), Messages.ALREADY_IGNORED.get());
        return;
      }

      player.addIgnore(targetAccount.get().getUniqueId());
      MessagesService.sendMessage(invocation.source(), Messages.ADD_IGNORE.get(target));
    } else if (invocation.arguments()[0].equalsIgnoreCase("remove")) {
      if (invocation.arguments().length < 2) {
        MessagesService.sendMessage(
            invocation.source(), Messages.INCORRECT_USAGE.get(invocation.source(), "/ignore remove <player>"));
        return;
      }

      Optional<ProxyChatAccount> targetAccount = AccountManager.getAccount(invocation.arguments()[1]);

      if (targetAccount.isEmpty()
          || (targetAccount.get().isVanished()
              && !PermissionManager.hasPermission(invocation.source(), Permission.COMMAND_VANISH_VIEW))) {
        MessagesService.sendMessage(invocation.source(), Messages.PLAYER_NOT_FOUND.get());
        return;
      }

      CommandSource target = ProxyChatAccountManager.getCommandSource(targetAccount.get()).orElseThrow();

      if (target == invocation.source()) {
        MessagesService.sendMessage(invocation.source(), Messages.UNIGNORE_YOURSELF.get());
        return;
      }

      if (!player.hasIgnored(targetAccount.get().getUniqueId())) {
        MessagesService.sendMessage(invocation.source(), Messages.NOT_IGNORED.get());
        return;
      }

      player.removeIgnore(targetAccount.get().getUniqueId());
      MessagesService.sendMessage(invocation.source(), Messages.REMOVE_IGNORE.get(target));
    } else {
      MessagesService.sendMessage(
          invocation.source(), Messages.INCORRECT_USAGE
                      .get(invocation.source(), "/ignore <list|add|remove> [player]"));
    }
  }

  @Override
  public List<String> suggest(Invocation invocation) {
    if(invocation.arguments().length == 0) {
      return arg1Completetions;
    }

    final String param1 = invocation.arguments()[0];

    if (invocation.arguments().length == 1) {
      return arg1Completetions.stream()
              .filter(completion -> completion.startsWith(invocation.arguments()[0]))
              .collect(Collectors.toList());
    }

    if(invocation.arguments().length == 2 && ("add".equals(param1) || "remove".equals(param1))) {
        return ProxyChatAccountManager.getUsernamesForPartialName(invocation.arguments()[1], invocation.source());
    }

    return super.suggest(invocation);
  }
}
