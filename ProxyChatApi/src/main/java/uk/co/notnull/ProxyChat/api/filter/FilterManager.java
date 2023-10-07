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

package uk.co.notnull.ProxyChat.api.filter;

import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;

public final class FilterManager {
  public static final int SWEAR_FILTER_PRIORITY = 100;
  public static final int ADVERTISING_FILTER_PRIORITY = 200;
  public static final int CAPSLOCK_FILTER_PRIORITY = 300;
  public static final int DUPLICATION_FILTER_PRIORITY = 400;
  public static final int SPAM_FILTER_PRIORITY = 500;
  public static final int LOCK_CHAT_FILTER_PRIORITY = 600;
  public static final int EMOJI_FILTER_PRIORITY = 700;

  private static Map<String, ProxyChatPreParseFilter> preParsefilters = new LinkedHashMap<>();
  private static Map<String, ProxyChatPostParseFilter> postParsefilters = new LinkedHashMap<>();

  public static void addPreParseFilter(String name, ProxyChatPreParseFilter filter) throws UnsupportedOperationException {
    preParsefilters.put(name, filter);

    sortFilters();
  }

  public static void addPostParseFilter(String name, ProxyChatPostParseFilter filter) throws UnsupportedOperationException {
    postParsefilters.put(name, filter);

    sortFilters();
  }

  public static void removePreParseFilter(String name) throws UnsupportedOperationException {
    preParsefilters.remove(name);
    sortFilters();
  }

  public static void removePostParseFilter(String name) throws UnsupportedOperationException {
    postParsefilters.remove(name);
    sortFilters();
  }

  public static String applyFilters(ProxyChatAccount sender, String message)
      throws UnsupportedOperationException, BlockMessageException {
    for (ProxyChatPreParseFilter filter : preParsefilters.values()) {
      message = filter.applyFilter(sender, message);
    }

    return message;
  }

  public static Component applyFilters(ProxyChatAccount sender, Component message)
      throws UnsupportedOperationException, BlockMessageException {
    for (ProxyChatPostParseFilter filter : postParsefilters.values()) {
      message = filter.applyFilter(sender, message);
    }

    return message;
  }

  private static void sortFilters() {
    preParsefilters =
        preParsefilters.entrySet().stream()
            .sorted(Collections.reverseOrder(Entry.comparingByValue()))
            .collect(
                Collectors.toMap(
                    Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

    postParsefilters =
        postParsefilters.entrySet().stream()
            .sorted(Collections.reverseOrder(Entry.comparingByValue()))
            .collect(
                Collectors.toMap(
                    Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }

  private FilterManager() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
