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

package uk.co.notnull.ProxyChat.api.hook;

import net.kyori.adventure.text.minimessage.MiniMessage;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

@UtilityClass
public class HookManager {
  public static final int DEFAULT_PREFIX_PRIORITY = 100;
  public static final int PERMISSION_PLUGIN_PREFIX_PRIORITY = 200;
  private static final MiniMessage miniMessage = MiniMessage.miniMessage();

  private static Map<String, ProxyChatHook> hooks = new LinkedHashMap<>();

  public static void addHook(String name, ProxyChatHook hook) {
    hooks.put(name, hook);

    sortHooks();
  }

  public static void removeHook(String name) {
    hooks.remove(name);
    sortHooks();
  }

  public String getPrefix(ProxyChatAccount account) {
    Optional<String> out;

    for (ProxyChatHook hook : hooks.values()) {
      out = hook.getPrefix(account);

      if (out.isPresent()) return out.get();
    }

    return "";
  }

  public String getSuffix(ProxyChatAccount account) {
    Optional<String> out;

    for (ProxyChatHook hook : hooks.values()) {
      out = hook.getSuffix(account);

      if (out.isPresent()) return out.get();
    }

    return "";
  }

  public String getFullName(ProxyChatAccount account) {
    return getPrefix(account) + account.getName() + getSuffix(account);
  }

  public String getFullDisplayName(ProxyChatAccount account) {
    return getPrefix(account) + account.getDisplayName() + getSuffix(account);
  }

  public Component getPrefixComponent(ProxyChatAccount account) {
    for (ProxyChatHook hook : hooks.values()) {
      Optional<String> prefix = hook.getPrefix(account);

      if(prefix.isPresent()) {
        return miniMessage.deserialize(prefix.get());
      }
    }

    return Component.empty();
  }

  public Component getSuffixComponent(ProxyChatAccount account) {
    for (ProxyChatHook hook : hooks.values()) {
      Optional<String> suffix = hook.getSuffix(account);

      if(suffix.isPresent()) {
        return miniMessage.deserialize(suffix.get());
      }
    }

    return Component.empty();
  }

  public Component getFullNameComponent(ProxyChatAccount account) {
    return miniMessage.deserialize(getPrefix(account) + account.getName() + getSuffix(account))
            .clickEvent(ClickEvent.suggestCommand("/w " + account.getName() + " "))
            .hoverEvent(Component.text("Click to whisper " + account.getName()));
  }

  public Component getFullDisplayNameComponent(ProxyChatAccount account) {
    return miniMessage.deserialize(getPrefix(account) + account.getDisplayName() + getSuffix(account))
            .clickEvent(ClickEvent.suggestCommand("/w " + account.getName() + " "))
            .hoverEvent(Component.text("Click to whisper " + account.getName()));
  }

  private static void sortHooks() {
    hooks =
        hooks.entrySet().stream()
            .sorted(Collections.reverseOrder(Entry.comparingByValue()))
            .collect(
                Collectors.toMap(
                    Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }
}
