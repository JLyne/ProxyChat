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

package uk.co.notnull.ProxyChat.listener;

import com.typesafe.config.Config;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import uk.co.notnull.ProxyChat.account.ProxyChatAccountManager;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.enums.ChannelType;
import uk.co.notnull.ProxyChat.api.utils.ChatUtils;
import uk.co.notnull.ProxyChat.message.Context;
import uk.co.notnull.ProxyChat.message.Messages;
import uk.co.notnull.ProxyChat.message.MessagesService;
import uk.co.notnull.ProxyChat.module.ProxyChatModuleManager;
import uk.co.notnull.ProxyChat.util.PredicateUtil;

import java.util.List;

public class LocalChatListener {
   private final boolean passToBackendServer = ProxyChatModuleManager.LOCAL_CHAT_MODULE
          .getModuleSection()
          .getBoolean("passToBackendServer");
  private final boolean passTransparently =
      ProxyChatModuleManager.LOCAL_CHAT_MODULE.getModuleSection().getBoolean("passTransparently");
  private final boolean logTransparentLocal =
      ProxyChatModuleManager.LOCAL_CHAT_MODULE
          .getModuleSection()
          .getBoolean("logTransparentLocal");
  private final Config serverListSection =
      ProxyChatModuleManager.LOCAL_CHAT_MODULE.getModuleSection().getConfig("passThruServerList");
  private final boolean serverListDisabled = !serverListSection.getBoolean("enabled");
  private final List<String> passthruServers = serverListSection.getStringList("list");

  @Subscribe
  public void onPlayerChat(PlayerChatEvent e) {
    if (!e.getResult().isAllowed()) return;
    if (e.getPlayer() == null) return;

    Player sender = e.getPlayer();
    ProxyChatAccount account = ProxyChatAccountManager.getAccount(sender).orElseThrow();
    String message = e.getMessage();

    if (ChatUtils.isCommand(message)) return;

    if (account.getChannelType() == ChannelType.LOCAL) {
      if (!PredicateUtil.getLocalChatEnabledPredicate().test(account)) {
        MessagesService.sendMessage(sender, Messages.NOT_IN_LOCAL_SERVER.get());
        e.setResult(PlayerChatEvent.ChatResult.denied());

        return;
      }

      // Check we send to this server
      boolean cancel = !passToBackendServer
              || (!serverListDisabled && !passthruServers.contains(account.getServerName()));

      e.setResult(cancel ? PlayerChatEvent.ChatResult.denied() : PlayerChatEvent.ChatResult.allowed());

      // Was just cancelled, or we want to process all local chat regardless
      if (cancel || !passTransparently) {
        MessagesService.sendLocalMessage(sender, message);
      }
      // still log and spy after transparently sent messages
      if (passTransparently && logTransparentLocal) {
        MessagesService.sendTransparentMessage(new Context(sender, message));
      }
    }
  }
}
