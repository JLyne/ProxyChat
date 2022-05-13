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

/**
 * This exception is not really an exception. It is used in filters to indicate that the message
 * should not be sent and instead that the sending user should be warned with the passed message.
 */
public class BlockMessageException extends Exception {
  private static final long serialVersionUID = -2629372445468034714L;

  /**
   * Construct a new {@link BlockMessageException} to indicate that the message should be blocked
   * and not sent but instead the sending user should be warned with the message passed.
   *
   * @param message The warning displayed to the user.
   */
  public BlockMessageException(String message) {
      super(message);
  }
}
