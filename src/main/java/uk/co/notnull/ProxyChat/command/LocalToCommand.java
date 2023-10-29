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

import com.velocitypowered.api.proxy.server.RegisteredServer;
import uk.co.notnull.ProxyChat.api.enums.ChannelType;
import uk.co.notnull.ProxyChat.api.placeholder.ProxyChatContext;
import uk.co.notnull.ProxyChat.message.Context;
import uk.co.notnull.ProxyChat.message.Messages;
import uk.co.notnull.ProxyChat.message.MessagesService;
import uk.co.notnull.ProxyChat.module.LocalToModule;
import uk.co.notnull.ProxyChat.api.permission.Permission;
import uk.co.notnull.ProxyChat.permission.PermissionManager;
import uk.co.notnull.ProxyChat.util.ServerNameUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LocalToCommand extends BaseCommand {
  public LocalToCommand(LocalToModule localToModule) {
    super(
        "localto",
        Permission.COMMAND_LOCALTO,
        localToModule.getModuleSection().getStringList("aliases"));
  }

  @Override
  public void execute(Invocation invocation) {
    if (!PermissionManager.hasPermission(invocation.source(), Permission.COMMAND_LOCALTO)) return;

    if (invocation.arguments().length < 2) {
      MessagesService.sendMessage(
          invocation.source(), Messages.INCORRECT_USAGE.get(invocation.source(), "/localto <server> <message>"));
      return;
    }

    Optional<RegisteredServer> server = ServerNameUtil.verifyServerName(invocation.arguments()[0], invocation.source());

    if (server.isEmpty()) return;

    String finalMessage = Arrays.stream(invocation.arguments(), 1, invocation.arguments().length)
            .collect(Collectors.joining(" "));
    ProxyChatContext context = new Context(invocation.source(), finalMessage, server.get());
    context.setChannel(ChannelType.LOCAL);
    MessagesService.parseMessage(context, true);

    MessagesService.sendChannelMessage(context);
  }

  @Override
  public List<String> suggest(Invocation invocation) {
    if(invocation.arguments().length == 0) {
      return ServerNameUtil.getServerNames();
    }

    if (invocation.arguments().length == 1) {
      return ServerNameUtil.getMatchingServerNames(invocation.arguments()[0]);
    }

    return super.suggest(invocation);
  }
}
