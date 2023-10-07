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

package uk.co.notnull.ProxyChat.api.module;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModuleManager {
  private static final List<ProxyChatModule> availableModules = new LinkedList<>();
  private static final List<ProxyChatModule> activeModules = new LinkedList<>();

  public static void registerModule(ProxyChatModule module) throws UnsupportedOperationException {
    availableModules.add(module);
  }

  public static List<ProxyChatModule> getActiveModules() throws UnsupportedOperationException {
    if (activeModules.isEmpty()) {
      activeModules.addAll(
          availableModules.stream()
              .filter(ProxyChatModule::isEnabled)
              .collect(Collectors.toList()));
    }

    return activeModules;
  }

  public static boolean isModuleActive(ProxyChatModule module)
      throws UnsupportedOperationException {
    return getActiveModules().contains(module);
  }

  public static Stream<ProxyChatModule> getAvailableModulesStream()
      throws UnsupportedOperationException {
    return getAvailableModules().stream();
  }

  public static Stream<ProxyChatModule> getActiveModulesStream()
      throws UnsupportedOperationException {
    return getActiveModules().stream();
  }

  public static void enableModules() throws UnsupportedOperationException {
    getActiveModulesStream().forEach(ProxyChatModule::onEnable);
  }

  public static void disableModules() throws UnsupportedOperationException {
    getActiveModulesStream().forEach(ProxyChatModule::onDisable);
  }

  public static void clearActiveModules() throws UnsupportedOperationException {
    activeModules.clear();
  }

  public static List<ProxyChatModule> getAvailableModules() {
    return ModuleManager.availableModules;
  }
}
