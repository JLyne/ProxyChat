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

import java.util.Objects;

/**
 * Called when a player has left the proxy, it is not safe to call any methods that perform an
 * action on the passed player instance.
 *
 * <p>Used by ProxyChat internally to make sure people joining while they are online don't cause
 * issues.
 */
public class ProxyChatLeaveEvent {
  /**
   * Player disconnecting.
   */
  private final Player player;

  public ProxyChatLeaveEvent(final Player player) {
    this.player = player;
  }

  /**
   * Player disconnecting.
   */
  public Player getPlayer() {
    return this.player;
  }

  @Override
  public String toString() {
    return "ProxyChatLeaveEvent(player=" + this.getPlayer() + ")";
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ProxyChatLeaveEvent other)) return false;
    if (!other.canEqual(this)) return false;
    final Object this$player = this.getPlayer();
    final Object other$player = other.getPlayer();
    return Objects.equals(this$player, other$player);
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ProxyChatLeaveEvent;
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
