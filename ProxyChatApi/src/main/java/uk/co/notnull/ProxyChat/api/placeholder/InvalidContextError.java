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

package uk.co.notnull.ProxyChat.api.placeholder;

import java.io.Serial;

/**
 * This exception is used to indicate that a passed context did not fulfill the requirements placed
 * on it.<br>
 * It is advised that when throwing this exception to provide information on what exact requirements
 * were not fulfilled as that simplifies debugging!
 */
public class InvalidContextError extends AssertionError {
  @Serial
  private static final long serialVersionUID = -7826893842156075019L;

  /**
   * Constructs a new error to indicate that a certain assertion or requirement of an {@link
   * ProxyChatContext} failed.
   *
   * @param message A message specifying what is wrong about the context, if possible
   */
  public InvalidContextError(String message) {
    super(message);
  }

  /**
   * Equivalent to calling InvalidContextError("Context does not meet all requirements!")
   *
   * @see InvalidContextError#InvalidContextError(String)
   */
  public InvalidContextError() {
    this("Context does not meet all requirements!");
  }
}
