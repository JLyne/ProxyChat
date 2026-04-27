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

package uk.co.notnull.ProxyChat.module;

import com.typesafe.config.Config;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import uk.co.notnull.ProxyChat.ProxyChat;
import uk.co.notnull.ProxyChat.listener.BridgeListener;

public class BridgeModule extends Module {
  private BridgeListener bridgeListener;
  private static final ChannelIdentifier channel = MinecraftChannelIdentifier.create("proxychat", "event");

  @Override
  public String getName() {
    return "Bridge";
  }

  @Override
  public void onEnable() {
    ProxyChat.getInstance().getProxy().getChannelRegistrar().register(channel);
    bridgeListener = new BridgeListener();

    ProxyChat.getInstance().getProxy()
        .getEventManager()
        .register(ProxyChat.getInstance(), bridgeListener);
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public Config getModuleSection() {
    return null;
  }

  public ChannelIdentifier getChannelIdentifier() {
    return channel;
  }

  @Override
  public void onDisable() {
    ProxyChat.getInstance().getProxy().getChannelRegistrar().unregister(channel);
    ProxyChat.getInstance().getProxy().getEventManager().unregisterListener(ProxyChat.getInstance(), bridgeListener);
  }
}
