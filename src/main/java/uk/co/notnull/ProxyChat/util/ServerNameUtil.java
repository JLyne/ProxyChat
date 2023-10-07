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

package uk.co.notnull.ProxyChat.util;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.TextComponent;
import uk.co.notnull.ProxyChat.ProxyChat;
import com.typesafe.config.Config;
import uk.co.notnull.ProxyChat.config.Configuration;
import uk.co.notnull.ProxyChat.message.Messages;
import uk.co.notnull.ProxyChat.message.MessagesService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ServerNameUtil {
  private static Map<String, String> aliasMapping = new HashMap<>();
  private static final Map<RegisteredServer, Component> serverComponents = new HashMap<>();
  private static final Map<RegisteredServer, Component> serverAliasComponents = new HashMap<>();

  public static Optional<ServerInfo> getServerInfo(String serverName) {
    Optional<RegisteredServer> server = ProxyChat.getInstance().getProxy().getAllServers().stream()
        .filter(s -> serverName.equalsIgnoreCase(s.getServerInfo().getName()))
        .findAny();

    return server.map(RegisteredServer::getServerInfo);
  }

  public static Optional<RegisteredServer> verifyServerName(String serverName) {
    return ProxyChat.getInstance().getProxy().getServer(serverName);
  }

  public static Optional<RegisteredServer> verifyServerName(String serverName, CommandSource sender) {
    final Optional<RegisteredServer> verifiedServer = verifyServerName(serverName);

    if(verifiedServer.isEmpty()) {
      MessagesService.sendMessage(sender, Messages.UNKNOWN_SERVER.get(sender, serverName));
    }

    return verifiedServer;
  }

  public static List<String> getServerNames() {
    return ProxyChat.getInstance().getProxy().getAllServers().stream().map(
            server -> server.getServerInfo().getName()).collect(Collectors.toList());
  }

  public static List<String> getMatchingServerNames(String partialName) {
    return getServerNames().stream()
        .filter(serverName -> serverName.startsWith(partialName))
        .collect(Collectors.toList());
  }

  public static String getServerAlias(ServerInfo server) {
    return getServerAlias(server.getName());
  }

  public static String getServerAlias(RegisteredServer server) {
    return aliasMapping.getOrDefault(server.getServerInfo().getName(), server.getServerInfo().getName());
  }

  public static String getServerAlias(String name) {
    return aliasMapping.getOrDefault(name, name);
  }

  public static Component getServerComponent(RegisteredServer server) {
    return serverComponents.getOrDefault(server, Component.empty());
  }

  public static Component getServerAliasComponent(RegisteredServer server) {
    return serverAliasComponents.getOrDefault(server, Component.empty());
  }

  public static void init() {
    Config section = Configuration.get().getConfig("ServerAlias");

    aliasMapping = section.root().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().unwrapped().toString()));

    ProxyChat.getInstance().getProxy().getAllServers().forEach(server -> {
      String name = server.getServerInfo().getName();
      TextComponent hoverEvent = Component.text().content(getServerAlias(name))
              .append(Component.newline())
              .append(Component.text("Click to join")
                              .color(NamedTextColor.YELLOW)).build();
      ClickEvent clickEvent = ClickEvent.runCommand("/server " + name);

      serverComponents.put(server, Component.text().content(name)
              .hoverEvent(hoverEvent).clickEvent(clickEvent).build());

      serverAliasComponents.put(server, Component.text().content(getServerAlias(name))
              .hoverEvent(hoverEvent).clickEvent(clickEvent).build());
    });
  }

  private ServerNameUtil() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
