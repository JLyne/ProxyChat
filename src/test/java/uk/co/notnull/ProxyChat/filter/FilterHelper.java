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

package uk.co.notnull.ProxyChat.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import uk.co.notnull.ProxyChat.api.account.AccountManager;
import uk.co.notnull.ProxyChat.api.filter.BlockMessageException;
import uk.co.notnull.ProxyChat.api.filter.ProxyChatFilter;
import uk.co.notnull.ProxyChat.message.Messages;

public class FilterHelper {
  private final Messages expectedMessage;

  public void assertException(ProxyChatFilter<String> filter, String text) {
    try {
      filter.applyFilter(AccountManager.getConsoleAccount(), text);

      fail("Expected exception!");
    } catch (ExtendedBlockMessageException e) {
      assertEquals("Exception Message is wrong", expectedMessage, e.getMessageType());
    } catch (BlockMessageException e) {
      fail("ExtendedBlockMessageException expected! (" + e.getMessage() + ')');
    }
  }

  public void assertNoException(ProxyChatFilter<String> filter, String text) {
    try {
      String result = filter.applyFilter(AccountManager.getConsoleAccount(), text);

      assertEquals("Message should not have been filtered", text, result);
    } catch (BlockMessageException e) {
      fail("No exception expected! (" + e.getMessage() + ')');
    }
  }

  public FilterHelper(final Messages expectedMessage) {
    this.expectedMessage = expectedMessage;
  }
}
