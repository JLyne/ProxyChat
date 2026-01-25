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

import static org.junit.Assert.assertEquals;

import uk.co.notnull.ProxyChat.api.placeholder.ProxyChatContext;
import uk.co.notnull.ProxyChat.api.placeholder.PlaceHolderManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class PlaceholdersTest {
  private static final long TIMEOUT = 1000;

  @BeforeClass
  public static void registerPlaceHolders() {
    PlaceHolders.registerPlaceHolders();
  }

  @Test
  public void registerPlaceholdersTest() {
    Assert.assertEquals(
			"Placeholders count is incorrect", 56L, PlaceHolderManager.getPlaceholderStream().count());
  }

  @Test(timeout = TIMEOUT)
  public void placeholderMessageEscapeTest() {
    final String message = "Test: &1 %message%";
    final ProxyChatContext context = new ProxyChatContext(message);

    // Note %message% gets replaced with the string "Test %message%"
    assertEquals(
        "Test: &1 Test: &1 %message%", PlaceHolderManager.processMessage(message, context));
  }
}
