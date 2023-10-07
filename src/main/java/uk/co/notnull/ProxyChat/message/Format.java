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

package uk.co.notnull.ProxyChat.message;

import uk.co.notnull.ProxyChat.api.placeholder.ProxyChatContext;
import net.kyori.adventure.text.Component;

public enum Format {
  ALERT("alert"),
  CHAT_LOGGING_CONSOLE("chatLoggingConsole"),
  CHAT_LOGGING_FILE("chatLoggingFile"),
  GLOBAL_CHAT("globalChat"),
  JOIN_MESSAGE("joinMessage"),
  LEAVE_MESSAGE("leaveMessage"),
  LOCAL_CHAT("localChat"),
  LOCAL_SPY("localSpy"),
  MESSAGE_SENDER("messageSender"),
  MESSAGE_TARGET("messageTarget"),
  MOTD("motd"),
  SERVER_SWITCH("serverSwitch"),
  SOCIAL_SPY("socialSpy"),
  STAFF_CHAT("staffChat"),
  WELCOME_MESSAGE("welcomeMessage");

  private final String stringPath;

  public Component get(ProxyChatContext context) {
    return PlaceHolderUtil.getFullFormatMessage(this, context);
  }

  public String getRaw(ProxyChatContext context) {
    return PlaceHolderUtil.getFullFormatMessageRaw(this, context);
  }

  private Format(final String stringPath) {
    this.stringPath = stringPath;
  }

  public String getStringPath() {
    return this.stringPath;
  }
}
