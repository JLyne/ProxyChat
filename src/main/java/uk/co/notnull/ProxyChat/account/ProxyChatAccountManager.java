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

package uk.co.notnull.ProxyChat.account;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import uk.co.notnull.ProxyChat.ProxyChat;
import uk.co.notnull.ProxyChat.api.account.AccountInfo;
import uk.co.notnull.ProxyChat.api.account.AccountManager;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.enums.AccountType;
import uk.co.notnull.ProxyChat.api.event.ProxyChatJoinEvent;
import uk.co.notnull.ProxyChat.api.event.ProxyChatLeaveEvent;

import uk.co.notnull.ProxyChat.api.permission.Permission;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProxyChatAccountManager extends AccountManager {
  private static final List<UUID> newPlayers = new LinkedList<>();

  public static Optional<ProxyChatAccount> getAccount(CommandSource player) {
    if (player instanceof Player) return getAccount(((Player) player).getUniqueId());
    else if (player == null) return Optional.empty();
    else return Optional.of(consoleAccount);
  }

  public static Optional<CommandSource> getCommandSource(UUID uuid) {
    ProxyServer instance = ProxyChat.getInstance().getProxy();

    if(uuid == null) {
      return Optional.empty();
    }

    if(uuid.equals(consoleAccount.getUniqueId())) {
      return Optional.of(instance.getConsoleCommandSource());
    } else {
      return Optional.ofNullable(instance.getPlayer(uuid).orElse(null));
    }
  }

  public static Optional<? extends CommandSource> getCommandSource(@NotNull ProxyChatAccount account) {
    ProxyServer instance = ProxyChat.getInstance().getProxy();

    return switch (account.getAccountType()) {
      case PLAYER -> instance.getPlayer(account.getUniqueId());
      default -> Optional.of(instance.getConsoleCommandSource());
    };
  }

  public static List<ProxyChatAccount> getAccountsForPartialName(String partialName, CommandSource player) {
    ProxyChatAccount playerAccount = getAccount(player).orElseThrow();
    return getAccountsForPartialName(partialName, playerAccount);
  }

  public static List<ProxyChatAccount> getAccountsForPartialName(String partialName, ProxyChatAccount account) {
    Stream<ProxyChatAccount> accounts = getAccountsForPartialName(partialName);

    if (!account.hasPermission(Permission.COMMAND_VANISH_VIEW)) {
      accounts = accounts.filter(a -> !a.equals(account)
              && a.getAccountType() == AccountType.PLAYER
              && !a.isVanished());
    }

    return accounts.collect(Collectors.toList());
  }

  public static List<String> getUsernamesForPartialName(String partialName, CommandSource player) {
    ProxyChatAccount playerAccount = getAccount(player).orElseThrow();
    return getUsernamesForPartialName(partialName, playerAccount);
  }

  public static List<String> getUsernamesForPartialName(String partialName, ProxyChatAccount account) {
    Stream<ProxyChatAccount> accounts = getAccountsForPartialName(partialName)
            .filter(a -> !a.equals(account) && a.getAccountType() == AccountType.PLAYER);

    if (!account.hasPermission(Permission.COMMAND_VANISH_VIEW)) {
      accounts = accounts.filter(a -> !a.isVanished());
    }

    return accounts.map(ProxyChatAccount::getName).collect(Collectors.toList());
  }

  public static void loadAccount(UUID uuid) {
    AccountInfo loadedAccount = getAccountStorage().load(uuid);

    accounts.put(uuid, loadedAccount.getAccount());

    if (loadedAccount.isForceSave()) {
      saveAccount(loadedAccount.getAccount());
    }

    if (loadedAccount.isNewAccount()) {
      newPlayers.add(loadedAccount.getAccount().getUniqueId());
    }
  }

  public static void unloadAccount(UUID uuid) {
    Optional<ProxyChatAccount> account = getAccount(uuid);

    account.ifPresent(
        acc -> {
          unloadAccount(acc);
          newPlayers.remove(acc.getUniqueId());
        });
  }

  public static void unloadAccount(ProxyChatAccount account) {
    AccountManager.unloadAccount(account);
  }

  public static boolean isNew(UUID uuid) {
    return newPlayers.contains(uuid);
  }

  @Subscribe(priority = Short.MAX_VALUE)
  public void onPlayerConnect(ProxyChatJoinEvent event) {
    loadAccount(event.getPlayer().getUniqueId());
  }

  @Subscribe(priority = Short.MIN_VALUE)
  public void onPlayerDisconnect(ProxyChatLeaveEvent event) {
    unloadAccount(event.getPlayer().getUniqueId());
  }
}
