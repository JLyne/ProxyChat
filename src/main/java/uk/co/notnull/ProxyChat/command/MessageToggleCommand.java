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
import uk.co.notnull.ProxyChat.api.account.AccountManager;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.enums.AccountType;
import uk.co.notnull.ProxyChat.message.Messages;
import uk.co.notnull.ProxyChat.message.MessagesService;
import uk.co.notnull.ProxyChat.module.MessengerModule;
import uk.co.notnull.ProxyChat.api.permission.Permission;
import uk.co.notnull.ProxyChat.permission.PermissionManager;

import java.util.List;
import java.util.Optional;

public class MessageToggleCommand extends BaseCommand {
  public MessageToggleCommand(MessengerModule messengerModule) {
    super(
			"msgtoggle",
			Permission.COMMAND_TOGGLE_MESSAGE,
			messengerModule.getModuleSection().getStringList("aliases.msgtoggle"));
  }

  @Override
  public void execute(Invocation invocation) {
    if (!PermissionManager.hasPermission(invocation.source(), Permission.COMMAND_TOGGLE_MESSAGE)) return;

    if (invocation.arguments().length == 0) {
      if (!(invocation.source() instanceof Player)) {
        MessagesService.sendMessage(invocation.source(), Messages.NOT_A_PLAYER.get());
        return;
      }

      ProxyChatAccount player = ProxyChatAccountManager.getAccount(invocation.source()).orElseThrow();
      player.toggleMessanger();

      if (player.hasMessangerEnabled()) {
        MessagesService.sendMessage(invocation.source(), Messages.ENABLE_MESSAGER.get());
      } else {
        MessagesService.sendMessage(invocation.source(), Messages.DISABLE_MESSAGER.get());
      }
    } else if (invocation.arguments().length == 1) {
      if (!PermissionManager.hasPermission(invocation.source(), Permission.COMMAND_TOGGLE_MESSAGE_OTHERS))
        return;

      Optional<ProxyChatAccount> targetAccount = AccountManager.getAccount(invocation.arguments()[0]);

      if (targetAccount.map(target -> target.getAccountType() != AccountType.PLAYER).orElse(true)) {
        MessagesService.sendMessage(invocation.source(), Messages.PLAYER_NOT_FOUND.get());
        return;
      }

      targetAccount.get().toggleMessanger();

      if (targetAccount.get().hasMessangerEnabled()) {
        MessagesService.sendMessage(
            invocation.source(), Messages.ENABLE_MESSAGER_OTHERS.get(targetAccount.get()));
      } else {
        MessagesService.sendMessage(
            invocation.source(), Messages.DISABLE_MESSAGER_OTHERS.get(targetAccount.get()));
      }
    } else {
      MessagesService.sendMessage(
          invocation.source(), Messages.INCORRECT_USAGE.get(invocation.source(), "/msgtoggle [player]"));
    }
  }

  @Override
  public List<String> suggest(Invocation invocation) {
    if (invocation.arguments().length == 1) {
      return ProxyChatAccountManager.getUsernamesForPartialName(invocation.arguments()[0], invocation.source());
    }

    return super.suggest(invocation);
  }
}
