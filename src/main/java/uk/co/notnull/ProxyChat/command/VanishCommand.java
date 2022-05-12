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
import uk.co.notnull.ProxyChat.message.Messages;
import uk.co.notnull.ProxyChat.message.MessagesService;
import uk.co.notnull.ProxyChat.module.VanishModule;
import uk.co.notnull.ProxyChat.api.permission.Permission;
import uk.co.notnull.ProxyChat.permission.PermissionManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VanishCommand extends BaseCommand {
  private static final List<String> arg1Completetions = Arrays.asList("on", "off");

  public VanishCommand(VanishModule vanisherModule) {
    super(
			"bvanish",
			Permission.COMMAND_VANISH,
			vanisherModule.getModuleSection().getStringList("aliases"));
  }

  @Override
  public void execute(Invocation invocation) {
    if (!PermissionManager.hasPermission(invocation.source(), Permission.COMMAND_VANISH)) return;

    if (!(invocation.source() instanceof Player)) {
      MessagesService.sendMessage(invocation.source(), Messages.NOT_A_PLAYER.get());
      return;
    }

    ProxyChatAccount player = ProxyChatAccountManager.getAccount(invocation.source()).get();

    if (invocation.arguments().length > 0) {
      if (invocation.arguments()[0].equalsIgnoreCase("on")) {
        player.setVanished(true);
      } else if (invocation.arguments()[0].equalsIgnoreCase("off")) {
        player.setVanished(false);
      } else {
        player.toggleVanished();
      }
    } else {
      player.toggleVanished();
    }

    if (player.isVanished()) {
      MessagesService.sendMessage(invocation.source(), Messages.ENABLE_VANISH.get());
    } else {
      MessagesService.sendMessage(invocation.source(), Messages.DISABLE_VANISH.get());
    }
  }

  @Override
  public List<String> suggest(Invocation invocation) {
    if(invocation.arguments().length == 0) {
      return arg1Completetions;
    }

    if (invocation.arguments().length == 1) {
      final String param1 = invocation.arguments()[0];

      return arg1Completetions.stream()
          .filter(completion -> completion.startsWith(param1))
          .collect(Collectors.toList());
    }

    return super.suggest(invocation);
  }
}
