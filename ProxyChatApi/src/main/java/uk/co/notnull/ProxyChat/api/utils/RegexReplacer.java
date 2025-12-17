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

package uk.co.notnull.ProxyChat.api.utils;

import org.intellij.lang.annotations.MagicConstant;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegexReplacer {
  private final String patternStr;
  private final Pattern pattern;
  private final String replacement;
  private final Map<Integer, Pattern> patternCache;

  public RegexReplacer(Pattern pattern, String replacement) {
    patternStr = pattern.pattern();
    this.pattern = pattern;
    this.replacement = replacement;

	  int defaultFlags = pattern.flags();
    patternCache = new HashMap<>();

    patternCache.put(defaultFlags, pattern);
  }

  public RegexReplacer(String pattern, String replacement) {
    this(pattern, 0, replacement);
  }

  public RegexReplacer(String pattern, @MagicConstant(flagsFromClass = Pattern.class) int regexFlags, String replacement) {
    this(Pattern.compile(pattern, regexFlags), replacement);
  }

  public String apply(String input) {
    return replaceAll(pattern, input);
  }

  public String apply(String input, @MagicConstant(flagsFromClass = Pattern.class) int flags) {
    if (!patternCache.containsKey(flags)) {
      patternCache.put(flags, Pattern.compile(patternStr, flags));
    }

    return replaceAll(patternCache.get(flags), input);
  }

  private String replaceAll(Pattern pattern, String input) {
    return pattern.matcher(input).replaceAll(replacement);
  }

  public String getPatternStr() {
    return this.patternStr;
  }

  public Pattern getPattern() {
    return this.pattern;
  }

  public String getReplacement() {
    return this.replacement;
  }
}
