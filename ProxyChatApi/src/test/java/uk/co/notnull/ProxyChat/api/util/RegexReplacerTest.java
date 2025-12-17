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

package uk.co.notnull.ProxyChat.api.util;

import static org.junit.Assert.assertEquals;

import uk.co.notnull.ProxyChat.api.utils.RegexReplacer;
import java.util.regex.Pattern;
import org.junit.Test;

public class RegexReplacerTest {
  private static final String PATTERN_SIMPLE = "foo";
  private static final String REPLACEMENT_SIMPLE = "bar";
  private static final String PATTERN_COMPLEX = "<([A-Z][A-Z0-9]*)\\b[^>]*>(.*?)</\\1>";
  private static final String REPLACEMENT_COMPLEX = "<$1_yay>$2</$1_yay>";

  @Test
  public void complexPatternMatching() {
    final RegexReplacer pattern =
        new RegexReplacer(PATTERN_COMPLEX, Pattern.CASE_INSENSITIVE, REPLACEMENT_COMPLEX);

    assertEquals(
        "Complex matching", "<f0o_yay>bar</f0o_yay>", pattern.apply("<f0o tags>bar</f0o>"));
  }

  @Test
  public void equalsTest() {
    final RegexReplacer pattern =
        new RegexReplacer(Pattern.compile(PATTERN_SIMPLE), REPLACEMENT_SIMPLE);
    final RegexReplacer string = new RegexReplacer(PATTERN_SIMPLE, REPLACEMENT_SIMPLE);

    assertEquals("Two pattern strings not equal", pattern.getPatternStr(), string.getPatternStr());
    assertEquals(
        "Two replacement strings not equal", pattern.getReplacement(), string.getReplacement());
    assertEquals(
        "Two patterns not equal", pattern.getPattern().toString(), string.getPattern().toString());
  }

  @Test
  public void flagsTest() {
    final RegexReplacer caseSensitive = new RegexReplacer(PATTERN_SIMPLE, REPLACEMENT_SIMPLE);
    final RegexReplacer caseInsensitive =
        new RegexReplacer(PATTERN_SIMPLE, Pattern.CASE_INSENSITIVE, REPLACEMENT_SIMPLE);

    final String notReplaced = "FoO";
    final String replaced = REPLACEMENT_SIMPLE;

	  assertEquals("caseSensitive original", notReplaced, caseSensitive.apply(notReplaced));
    assertEquals(
        "caseSensitive reflagged", replaced, caseSensitive.apply(notReplaced, Pattern.CASE_INSENSITIVE));
    assertEquals("caseInsensitive original", replaced, caseInsensitive.apply(notReplaced));
    assertEquals("caseInsensitive reflagged", notReplaced, caseInsensitive.apply(notReplaced, 0));

    // Repeat to check cache
    assertEquals("caseSensitive original", notReplaced, caseSensitive.apply(notReplaced));
    assertEquals(
        "caseSensitive reflagged", replaced, caseSensitive.apply(notReplaced, Pattern.CASE_INSENSITIVE));
    assertEquals("caseInsensitive original", replaced, caseInsensitive.apply(notReplaced));
    assertEquals("caseInsensitive reflagged", notReplaced, caseInsensitive.apply(notReplaced, 0));
  }
}
