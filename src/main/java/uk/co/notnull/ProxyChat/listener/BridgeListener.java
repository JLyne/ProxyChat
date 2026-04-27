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

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import uk.co.notnull.ProxyChat.api.account.AccountManager;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.enums.ChannelType;
import uk.co.notnull.ProxyChat.api.placeholder.ProxyChatContext;
import uk.co.notnull.ProxyChat.message.MessagesService;
import uk.co.notnull.ProxyChat.module.ProxyChatModuleManager;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class BridgeListener {
  private static final GsonComponentSerializer serializer = GsonComponentSerializer.gson();

  @Subscribe
  public void onPluginMessage(PluginMessageEvent e) {
    if (!e.getIdentifier().equals(ProxyChatModuleManager.BRIDGE_MODULE.getChannelIdentifier())) {
      return;
    }

    e.setResult(PluginMessageEvent.ForwardResult.handled());

    if (!(e.getSource() instanceof ServerConnection connection)) {
      return;
    }

    Optional<ProxyChatAccount> account = AccountManager.getAccount(connection.getPlayer().getUniqueId());

    if (account.isEmpty()) {
      return;
    }

    Component message = serializer.deserialize(new String(e.getData(), StandardCharsets.UTF_8));
    ProxyChatContext context = new ProxyChatContext();
    context.setMessage("Fallback TODO");
    context.setParsedMessage(message);
    context.setServer(connection.getServer());
    context.setSender(account.get());
    context.setChannel(ChannelType.LOCAL_EVENT);

    MessagesService.sendChannelMessage(context);
  }
}
