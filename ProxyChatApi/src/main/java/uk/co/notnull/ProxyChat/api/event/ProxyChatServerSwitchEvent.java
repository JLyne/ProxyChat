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

package uk.co.notnull.ProxyChat.api.event;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

/**
 * Called when a player has changed servers.
 *
 * <p>Used by ProxyChat internally to make sure people joining while they are online don't cause
 * issues.
 */
public class ProxyChatServerSwitchEvent {
  /**
   * Player whom the server is for.
   */
  private final Player player;
  /**
   * Server the player is switch from.
   */
  private final RegisteredServer from;

  /**
   * Player whom the server is for.
   */
  public Player getPlayer() {
    return this.player;
  }

  /**
   * Server the player is switch from.
   */
  public RegisteredServer getFrom() {
    return this.from;
  }

  public ProxyChatServerSwitchEvent(final Player player, final RegisteredServer from) {
    this.player = player;
    this.from = from;
  }

  @Override
  public String toString() {
    return "ProxyChatServerSwitchEvent(player=" + this.getPlayer() + ", from=" + this.getFrom() + ")";
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ProxyChatServerSwitchEvent)) return false;
    final ProxyChatServerSwitchEvent other = (ProxyChatServerSwitchEvent) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$player = this.getPlayer();
    final Object other$player = other.getPlayer();
    if (this$player == null ? other$player != null : !this$player.equals(other$player)) return false;
    final Object this$from = this.getFrom();
    final Object other$from = other.getFrom();
    if (this$from == null ? other$from != null : !this$from.equals(other$from)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ProxyChatServerSwitchEvent;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $player = this.getPlayer();
    result = result * PRIME + ($player == null ? 43 : $player.hashCode());
    final Object $from = this.getFrom();
    result = result * PRIME + ($from == null ? 43 : $from.hashCode());
    return result;
  }
}
