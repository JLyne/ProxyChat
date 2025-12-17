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
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import uk.co.notnull.ProxyChat.account.ProxyChatAccountManager;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.enums.ChannelType;
import uk.co.notnull.ProxyChat.message.Messages;
import uk.co.notnull.ProxyChat.message.MessagesService;

public class MutingListener {
  @Subscribe(priority = Short.MAX_VALUE)
    public void onPlayerChat(PlayerChatEvent e) {
    if (!e.getResult().isAllowed()) return;
    if (e.getPlayer() == null) return;

    Player sender = e.getPlayer();
    ProxyChatAccount account = ProxyChatAccountManager.getAccount(sender).orElseThrow();

    if (!account.isMuted()) return;

    final ChannelType channel = account.getChannelType();

    if (channel == ChannelType.STAFF) return;

    e.setResult(PlayerChatEvent.ChatResult.denied());
    MessagesService.sendMessage(sender, Messages.MUTED.get(account));
  }
}
