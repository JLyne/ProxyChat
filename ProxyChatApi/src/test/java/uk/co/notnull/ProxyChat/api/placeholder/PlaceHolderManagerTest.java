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

import static org.junit.Assert.assertEquals;

import net.kyori.adventure.text.Component;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Objects;

public class PlaceHolderManagerTest {
  private static final long TIMEOUT = 1000;
  private static final ProxyChatContext EMPTY_CONTEXT = new ProxyChatContext();

  @BeforeClass
  public static void registerPlaceHolders() {
    PlaceHolderManager.registerPlaceholder(
        new HelperPlaceholder("test", "HAIII"),
        new HelperPlaceholder("recursive1", "xxx %test% xxx"),
        new HelperPlaceholder("recursive2", "hihi %recursive1% hihi"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void duplicatePlaceholderTest() {
    final HelperPlaceholder placeholder = new HelperPlaceholder("dummy", null);

    PlaceHolderManager.registerPlaceholder(placeholder, placeholder);
  }

  @Test(timeout = TIMEOUT)
  public void escapeTest() {
    final String message = "Test %% Test";

    assertEquals("Test % Test", PlaceHolderManager.processMessage(message, EMPTY_CONTEXT));
  }

  @Test(timeout = TIMEOUT)
  public void hangingPlaceholderTest() {
    final String message = "Test %xxx";

    assertEquals(message, PlaceHolderManager.processMessage(message, EMPTY_CONTEXT));
  }

  @Test(timeout = TIMEOUT)
  public void hangingDelimiterTest() {
    final String message = "Test %";

    assertEquals(message, PlaceHolderManager.processMessage(message, EMPTY_CONTEXT));
  }

  @Test(timeout = TIMEOUT)
  public void unknownPlaceholderTest() {
    final String message = "Test %xxx% %hi%";

    assertEquals(message, PlaceHolderManager.processMessage(message, EMPTY_CONTEXT));
  }

  @Test(timeout = TIMEOUT)
  public void placeholderTest() {
    final String message = "Test %test% Test";

    assertEquals("Test HAIII Test", PlaceHolderManager.processMessage(message, EMPTY_CONTEXT));
  }

  @Test(timeout = TIMEOUT)
  public void recusivePlaceholderTest() {
    final String message = "Test %recursive2% Test";

    assertEquals(
        "Test hihi xxx HAIII xxx hihi Test",
        PlaceHolderManager.processMessage(message, EMPTY_CONTEXT));
  }


  private static final class HelperPlaceholder implements ProxyChatPlaceHolder {
    private final String name;
    private final String replacement;

    @Override
    public boolean isContextApplicable(ProxyChatContext context) {
      return true;
    }

    @Override
    public Component getReplacementComponent(String name, ProxyChatContext context) {
      return Component.text(replacement);
    }

    @Override
    public String getReplacement(String name, ProxyChatContext context) {
      return replacement;
    }

    public HelperPlaceholder(final String name, final String replacement) {
      this.name = name;
      this.replacement = replacement;
    }

    public String getName() {
      return this.name;
    }

    public String getReplacement() {
      return this.replacement;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof HelperPlaceholder other)) return false;
      final Object this$name = this.getName();
      final Object other$name = other.getName();
      if (!Objects.equals(this$name, other$name)) return false;
      final Object this$replacement = this.getReplacement();
      final Object other$replacement = other.getReplacement();
      return Objects.equals(this$replacement, other$replacement);
	}

    @Override
    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final Object $name = this.getName();
      result = result * PRIME + ($name == null ? 43 : $name.hashCode());
      final Object $replacement = this.getReplacement();
      result = result * PRIME + ($replacement == null ? 43 : $replacement.hashCode());
      return result;
    }

    @Override
    public String toString() {
      return "PlaceHolderManagerTest.HelperPlaceholder(name=" + this.getName() + ", replacement=" + this.getReplacement() + ")";
    }
  }
}
