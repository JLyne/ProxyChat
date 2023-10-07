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

import java.util.function.Function;

public class FunctionFilter implements ProxyChatPreParseFilter {
  private final Function<String, String> filter;
  private final int priority;

  public FunctionFilter(Function<String, String> filter) {
    this(filter, 0);
  }

  @Override
  public String applyFilter(ProxyChatAccount sender, String message) {
    return filter.apply(message);
  }

  public FunctionFilter(final Function<String, String> filter, final int priority) {
    this.filter = filter;
    this.priority = priority;
  }

  public int getPriority() {
    return this.priority;
  }
}
