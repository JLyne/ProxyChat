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

/**
 * Event called as soon as a connection has a {@link Player} and is ready to be connected to
 * a server.
 *
 * <p>Used by ProxyChat internally to make sure people joining while they are online don't cause
 * issues.
 */
public class ProxyChatJoinEvent {
  /**
   * The player involved with this event.
   */
  private final Player player;

  public ProxyChatJoinEvent(final Player player) {
    this.player = player;
  }

  /**
   * The player involved with this event.
   */
  public Player getPlayer() {
    return this.player;
  }

  @Override
  public String toString() {
    return "ProxyChatJoinEvent(player=" + this.getPlayer() + ")";
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ProxyChatJoinEvent)) return false;
    final ProxyChatJoinEvent other = (ProxyChatJoinEvent) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$player = this.getPlayer();
    final Object other$player = other.getPlayer();
    if (this$player == null ? other$player != null : !this$player.equals(other$player)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ProxyChatJoinEvent;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $player = this.getPlayer();
    result = result * PRIME + ($player == null ? 43 : $player.hashCode());
    return result;
  }
}
