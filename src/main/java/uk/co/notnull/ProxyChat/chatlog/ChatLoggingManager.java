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

package uk.co.notnull.ProxyChat.chatlog;

import uk.co.notnull.ProxyChat.ProxyChat;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.enums.ChannelType;
import uk.co.notnull.ProxyChat.api.placeholder.ProxyChatContext;
import uk.co.notnull.ProxyChat.api.utils.RegexUtil;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ChatLoggingManager {
  private static final List<ChatLogger> loggers = new LinkedList<>();
  private static List<Pattern> filteredCommands = new LinkedList<>();

  public static void addLogger(ChatLogger logger) {
    loggers.add(logger);
  }

  public static void removeLogger(ChatLogger logger) {
    loggers.remove(logger);
  }

  public static void logMessage(String channel, ProxyChatAccount sender, String message) {
    ProxyChatContext context = new ProxyChatContext(sender, message);
    context.setChannel(channel);

    logMessage(context);
  }

  public static void logMessage(ChannelType channel, ProxyChatContext context) {
    logMessage(channel.name(), context);

    if(ProxyChat.getInstance().getProxyDiscordHandler() != null) {
      ProxyChat.getInstance().getProxyDiscordHandler().logMessage(channel, context);
    }
  }

  public static void logMessage(String channel, ProxyChatContext context) {
    context.setChannel(channel);

    logMessage(context);
  }

  public static void logMessage(ProxyChatContext context) {
    context.require(
            ProxyChatContext.HAS_SENDER, ProxyChatContext.HAS_MESSAGE, ProxyChatContext.HAS_CHANNEL);

    getStream().forEach(logger -> logger.log(context));
  }

  public static void logCommand(ProxyChatAccount account, String command) {
    for (Pattern pattern : filteredCommands) {
      if (pattern.matcher(command).find()) return;
    }

//    logMessage("COMMAND", account, command);
  }

  public static void loadFilteredCommands(List<String> commands) {
    filteredCommands =
        commands.stream()
            .map(RegexUtil::parseWildcardToPattern)
            .collect(Collectors.toList());
  }

  private static Stream<ChatLogger> getStream() {
    return loggers.stream();
  }

  private ChatLoggingManager() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
