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

package uk.co.notnull.ProxyChat.api.account;

import java.util.UUID;

public interface ProxyChatAccountStorage {
  void save(ProxyChatAccount account);

  /**
   * Load a player with the given UUID from the data source this class represents.
   *
   * @param uuid UUID of the player to load.
   * @return A tuple of the player and a boolean that specifies whether it needs to be saved right
   *     after.
   */
  AccountInfo load(UUID uuid);

  default boolean requiresConsoleAccountSave() {
    return false;
  }
}
