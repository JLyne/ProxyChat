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

package uk.co.notnull.ProxyChat.api.enums;

import uk.co.notnull.ProxyChat.api.placeholder.InvalidContextError;
import uk.co.notnull.ProxyChat.api.placeholder.ProxyChatContext;

import java.util.function.Predicate;

/**
 * An Enum that contains all channel types.<br>
 * This is used to differentiate in which channel a person is talking, and the message needs to be
 * replicated.
 */
public enum ChannelType {
  GLOBAL(true, true, false),
  LOCAL(true, true, false, ProxyChatContext.HAS_MESSAGE, ProxyChatContext.IS_PARSED, ProxyChatContext.HAS_NO_TARGET, ProxyChatContext.HAS_SERVER),
  MULTICAST(true, false, false, ProxyChatContext.HAS_MESSAGE, ProxyChatContext.IS_PARSED, ProxyChatContext.HAS_NO_TARGET, ProxyChatContext.HAS_SERVER),
  STAFF(false, true, false),
  PRIVATE(true, true, false, ProxyChatContext.HAS_TARGET, ProxyChatContext.HAS_MESSAGE, ProxyChatContext.IS_PARSED),
  JOIN(false, false, true, ProxyChatContext.HAS_NO_MESSAGE, ProxyChatContext.HAS_NO_TARGET),
  LEAVE(false, false, true, ProxyChatContext.HAS_NO_MESSAGE, ProxyChatContext.HAS_NO_TARGET),
  SWITCH(false, false, true, ProxyChatContext.HAS_NO_MESSAGE, ProxyChatContext.HAS_SERVER, ProxyChatContext.HAS_NO_TARGET),
  ALERT(false, true, false);

  private final boolean ignorable;
  private final boolean loggable;
  private final boolean respectVanish;
  private Predicate<ProxyChatContext>[] requirements = new Predicate[]{ProxyChatContext.HAS_MESSAGE, ProxyChatContext.IS_PARSED, ProxyChatContext.HAS_NO_TARGET};

  ChannelType(boolean ignorable, boolean loggable, boolean respectVanish) {
    this.ignorable = ignorable;
    this.loggable = loggable;
    this.respectVanish = respectVanish;
  }

  ChannelType(boolean ignorable, boolean loggable, boolean respectVanish, Predicate<ProxyChatContext> ...requirements) {
    this.ignorable = ignorable;
    this.loggable = loggable;
    this.respectVanish = respectVanish;
    this.requirements = requirements;
  }

  public boolean isIgnorable() {
    return ignorable;
  }

  public boolean isLoggable() {
    return loggable;
  }

  public boolean isRespectVanish() {
    return respectVanish;
  }

  public Predicate<ProxyChatContext>[] getRequirements() {
    return requirements;
  }

  public void checkRequirements(ProxyChatContext context) throws InvalidContextError {
    context.require(ProxyChatContext.HAS_CHANNEL, ProxyChatContext.HAS_SENDER);
    context.require(requirements);
  }

  @Override
  public String toString() {
    return "ChannelType";
  }
}
