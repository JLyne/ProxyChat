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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import uk.co.notnull.ProxyChat.api.ProxyChatApi;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.enums.ChannelType;
import uk.co.notnull.ProxyChat.api.placeholder.ProxyChatContext;
import uk.co.notnull.ProxyChat.api.placeholder.InvalidContextError;
import uk.co.notnull.ProxyChat.api.utils.ProxyChatInstaceHolder;
import java.io.File;
import org.junit.BeforeClass;
import org.junit.Test;

public class FilterManagerTest {
  @BeforeClass
  public static void setupApi() {
    ProxyChatInstaceHolder.setInstance(
        new ProxyChatApi() {
          @Override
          public void sendPrivateMessage(ProxyChatContext context) throws InvalidContextError {
            // Nothing
          }

          @Override
          public void sendChannelMessage(ProxyChatContext context, ChannelType channel)
              throws InvalidContextError {
            // Nothing
          }

          @Override
          public File getConfigFolder() {
            return null;
          }
        });
  }

  @Test
  public void executionTest() {
    final String message = "test";

    ProxyChatPreParseFilter filter = new TestFilter(message);

    try {
      FilterManager.addPreParseFilter(message, filter);

      FilterManager.applyFilters(null, message);

      fail("Filter has not be called!");
    } catch (BlockMessageException e) {
      assertEquals("Exception message not as expected!", message, e.getMessage());
    } finally {
      FilterManager.removePreParseFilter(message);
    }
  }

  @Test
  public void orderTest() throws BlockMessageException {
    final String message1 = "test100";
    final String message2 = "test200";
    final String message3 = "test300";
    final String message4 = "test400";

    ProxyChatPreParseFilter filter1 = new FunctionFilter(in -> in + '1', 100);
    ProxyChatPreParseFilter filter2 = new FunctionFilter(in -> in + '2', 200);
    ProxyChatPreParseFilter filter3 = new FunctionFilter(in -> in + '3', 300);
    ProxyChatPreParseFilter filter4 = new FunctionFilter(in -> in + '4', 400);

    try {
      FilterManager.addPreParseFilter(message1, filter1);
      FilterManager.addPreParseFilter(message2, filter2);
      FilterManager.addPreParseFilter(message3, filter3);
      FilterManager.addPreParseFilter(message4, filter4);

      String ret = FilterManager.applyFilters(null, "Test_");

      assertEquals("Result message not as expected!", "Test_4321", ret);
    } finally {
      FilterManager.removePreParseFilter(message1);
      FilterManager.removePreParseFilter(message2);
      FilterManager.removePreParseFilter(message3);
      FilterManager.removePreParseFilter(message4);
    }
  }

  @Test
  public void priorityTest() {
    final String message1 = "test100";
    final String message2 = "test200";
    final String message3 = "test300";
    final String message4 = "test400";

    ProxyChatPreParseFilter filter1 = new TestFilter(message1, 100);
    ProxyChatPreParseFilter filter2 = new TestFilter(message2, 200);
    ProxyChatPreParseFilter filter3 = new TestFilter(message3, 300);
    ProxyChatPreParseFilter filter4 = new TestFilter(message4, 400);

    try {
      FilterManager.addPreParseFilter(message1, filter1);
      FilterManager.addPreParseFilter(message2, filter2);
      FilterManager.addPreParseFilter(message3, filter3);
      FilterManager.addPreParseFilter(message4, filter4);

      FilterManager.applyFilters(null, (String) null);

      fail("Filter has not be called!");
    } catch (BlockMessageException e) {
      assertEquals("Exception message not as expected!", message4, e.getMessage());
    } finally {
      FilterManager.removePreParseFilter(message1);
      FilterManager.removePreParseFilter(message2);
      FilterManager.removePreParseFilter(message3);
      FilterManager.removePreParseFilter(message4);
    }
  }


  private static class TestFilter implements ProxyChatPreParseFilter {
    private final String message;
    private final int priority;

    public TestFilter(String message) {
      this(message, 0);
    }

    @Override
    public String applyFilter(ProxyChatAccount sender, String message)
        throws BlockMessageException {
      throw new BlockMessageException(this.message);
    }

    public TestFilter(final String message, final int priority) {
      this.message = message;
      this.priority = priority;
    }

    public String getMessage() {
      return this.message;
    }

    public int getPriority() {
      return this.priority;
    }
  }
}
