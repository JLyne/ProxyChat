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

package uk.co.notnull.ProxyChat.message;

import com.velocitypowered.api.proxy.Player;
import uk.co.notnull.ProxyChat.ProxyChat;
import uk.co.notnull.ProxyChat.account.ProxyChatAccountManager;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.enums.AccountType;
import uk.co.notnull.ProxyChat.api.hook.HookManager;
import uk.co.notnull.ProxyChat.api.placeholder.ProxyChatContext;
import uk.co.notnull.ProxyChat.api.placeholder.ComponentReplacementSupplier;
import uk.co.notnull.ProxyChat.api.placeholder.PlaceHolder;
import uk.co.notnull.ProxyChat.api.placeholder.PlaceHolderManager;
import uk.co.notnull.ProxyChat.api.utils.TimeUtil;
import uk.co.notnull.ProxyChat.util.ServerNameUtil;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import net.kyori.adventure.text.Component;

public final class PlaceHolders {
  private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public static void registerPlaceHolders() {
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder("timestamp", context -> TimeUtil.getLongTimeStamp()));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder("time", context -> TimeUtil.getTimeStamp()));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder("short_time", context -> TimeUtil.getShortTimeStamp()));
    PlaceHolderManager.registerPlaceholder(new PlaceHolder("date", context -> TimeUtil.getDate()));
    PlaceHolderManager.registerPlaceholder(new PlaceHolder("day", context -> TimeUtil.getDay()));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder("month", context -> TimeUtil.getMonth()));
    PlaceHolderManager.registerPlaceholder(new PlaceHolder("year", context -> TimeUtil.getYear()));

    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "name",
                context -> context.getSender().get().getName(),
                ProxyChatContext.HAS_SENDER)
            .createAliases("sender_name"));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "displayname",
                context -> context.getSender().get().getDisplayName(),
                ProxyChatContext.HAS_SENDER)
            .createAliases("sender_displayname"));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "fullname",
                context -> HookManager.getFullName(context.getSender().get()),
                (ComponentReplacementSupplier) context -> HookManager.getFullNameComponent(context.getSender().get()),
                ProxyChatContext.HAS_SENDER)
            .createAliases("sender_fullname"));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "fulldisplayname",
                context -> HookManager.getFullDisplayName(context.getSender().get()),
                (ComponentReplacementSupplier) context -> HookManager.getFullDisplayNameComponent(context.getSender().get()),
                ProxyChatContext.HAS_SENDER)
            .createAliases("sender_displayfullname"));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "prefix",
                context -> HookManager.getPrefix(context.getSender().get()),
                (ComponentReplacementSupplier) context -> HookManager.getPrefixComponent(context.getSender().get()),
                ProxyChatContext.HAS_SENDER)
            .createAliases("sender_prefix"));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "suffix",
                context -> HookManager.getSuffix(context.getSender().get()),
                (ComponentReplacementSupplier) context -> HookManager.getSuffixComponent(context.getSender().get()),
                ProxyChatContext.HAS_SENDER)
            .createAliases("sender_suffix"));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "ping",
                context -> String.valueOf(context.getSender().get().getPing()),
                ProxyChatContext.HAS_SENDER)
            .createAliases("sender_ping"));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "uuid",
                context -> context.getSender().get().getUniqueId().toString(),
                ProxyChatContext.HAS_SENDER)
            .createAliases("sender_uuid"));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "servername",
                context -> context.getSender().get().getServer()
                        .map(server -> server.getServerInfo().getName()).orElse(ProxyChatAccount.unknownServer),
                (ComponentReplacementSupplier) context -> context.getSender().get()
                        .getServer().map(ServerNameUtil::getServerComponent)
                        .orElse(Component.text(ProxyChatAccount.unknownServer)),
                ProxyChatContext.HAS_SENDER)
            .createAliases("sender_servername", "to_servername"));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "serveralias",
                context -> ServerNameUtil.getServerAlias(context.getSender().get().getServer().get()),
                (ComponentReplacementSupplier) context -> context.getSender().get()
                        .getServer().map(ServerNameUtil::getServerAliasComponent)
                        .orElse(Component.text(ProxyChatAccount.unknownServer)),
                ProxyChatContext.HAS_SENDER)
            .createAliases("sender_serveralias", "to_serveralias"));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "serverip",
                context -> context.getSender().get().getServerIP(),
                ProxyChatContext.HAS_SENDER)
            .createAliases("sender_serverip", "to_serverip"));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "from_servername",
            context ->
                context.getServer().map(server -> server.getServerInfo().getName())
                    .orElse(ProxyChatAccount.unknownServer),
                (ComponentReplacementSupplier) context -> context.getServer()
                    .map(ServerNameUtil::getServerComponent).orElse(Component.text(ProxyChatAccount.unknownServer)),
                ProxyChatContext.HAS_SERVER));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "from_serveralias",
            context ->
                context.getSender().get().getServer().map(ServerNameUtil::getServerAlias)
                    .orElse(ProxyChatAccount.unknownServer),
                (ComponentReplacementSupplier) context -> context.getSender().get().getServer()
                    .map(ServerNameUtil::getServerAliasComponent).orElse(Component.text(ProxyChatAccount.unknownServer)),
                ProxyChatContext.HAS_SERVER));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "from_serverip",
            context ->
                context.getSender().get().getServer()
                        .map(server -> server.getServerInfo().getAddress())
                        .map(SocketAddress::toString)
                        .orElse(ProxyChatAccount.unknownServer),
                ProxyChatContext.HAS_SERVER));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "server_online",
                context -> getLocalPlayerCount(context.getSender().get()),
                ProxyChatContext.HAS_SENDER)
            .createAliases("sender_server_online"));

    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "target_name",
            context -> context.getTarget().get().getName(),
                ProxyChatContext.HAS_TARGET));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "target_displayname",
            context -> context.getTarget().get().getDisplayName(),
                ProxyChatContext.HAS_TARGET));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "target_fullname",
            context -> HookManager.getFullName(context.getTarget().get()),
                ProxyChatContext.HAS_TARGET));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "target_fulldisplayname",
            context -> HookManager.getFullDisplayName(context.getTarget().get()),
                ProxyChatContext.HAS_TARGET));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "target_prefix",
            context -> HookManager.getPrefix(context.getTarget().get()),
                ProxyChatContext.HAS_TARGET));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "target_suffix",
            context -> HookManager.getSuffix(context.getTarget().get()),
                ProxyChatContext.HAS_TARGET));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "target_ping",
            context -> String.valueOf(context.getTarget().get().getPing()),
                ProxyChatContext.HAS_TARGET));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "target_uuid",
            context -> context.getTarget().get().getUniqueId().toString(),
                ProxyChatContext.HAS_TARGET));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "target_servername",
            context -> context.getTarget().get().getServerName(),
            (ComponentReplacementSupplier) context -> context.getTarget().get().getServer()
                    .map(ServerNameUtil::getServerComponent).orElse(Component.text(ProxyChatAccount.unknownServer)),
                ProxyChatContext.HAS_TARGET));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "target_serveralias",
            context -> ServerNameUtil.getServerAlias(context.getTarget().get().getServerName()),
            (ComponentReplacementSupplier) context -> context.getTarget().get().getServer()
                    .map(ServerNameUtil::getServerAliasComponent).orElse(Component.text(ProxyChatAccount.unknownServer)),
                ProxyChatContext.HAS_TARGET));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "target_serverip",
            context -> context.getTarget().get().getServerIP(),
                ProxyChatContext.HAS_TARGET));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "target_server_online",
            context -> getLocalPlayerCount(context.getTarget().get()),
                ProxyChatContext.HAS_TARGET));

    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "channel", context -> context.getChannel().get().name(), ProxyChatContext.HAS_CHANNEL));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder(
                "message",
                context -> PlaceHolderUtil.escapePlaceholders(context.getFilteredMessage().orElse(context.getMessage().get())),
                (ComponentReplacementSupplier) context -> context.getParsedMessage().get(),
                ProxyChatContext.HAS_MESSAGE)
            .createAliases("command", "unknown_server"));
    PlaceHolderManager.registerPlaceholder(
        new PlaceHolder("network_online", context -> getTotalPlayerCount()));
    PlaceHolderManager.registerPlaceholder(
            new PlaceHolder("plugin_prefix",
                            context -> Messages.PLUGIN_PREFIX.getRaw(),
                            (ComponentReplacementSupplier) context -> Messages.PLUGIN_PREFIX.get()));
  }

  private static String getLocalPlayerCount(ProxyChatAccount player) {
    if (player.getAccountType() == AccountType.CONSOLE) return getTotalPlayerCount();

    Player nativePlayer = (Player) ProxyChatAccountManager.getCommandSource(player).orElseThrow();

    return Integer.toString(nativePlayer.getCurrentServer().get().getServer().getPlayersConnected().size());
  }

  private static String getTotalPlayerCount() {
    return Integer.toString(ProxyChat.getInstance().getProxy().getAllPlayers().size());
  }

  private static SimpleDateFormat getDateFormat() {
    return new SimpleDateFormat(dateFormat);
  }

  private PlaceHolders() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
