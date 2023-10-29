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

import java.util.List;

public class MulticastChatModule extends Module {
  private List<List<String>> multiCastServerGroups = null;

  @Override
  public String getName() {
    return "MulticastChat";
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onEnable() {
    multiCastServerGroups = getModuleSection().getList("serverLists").stream()
            .map(configValue -> (List<String>) configValue.unwrapped())
            .toList();
  }

  @Override
  public void onDisable() {
    multiCastServerGroups = null;
  }

  public List<List<String>> getMultiCastServerGroups() {
    return multiCastServerGroups;
  }
}
