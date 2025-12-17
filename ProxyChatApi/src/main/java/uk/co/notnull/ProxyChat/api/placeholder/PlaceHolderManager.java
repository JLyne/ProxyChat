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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;

public final class PlaceHolderManager {
  public static final Character placeholderChar = '%';
  private static final Pattern placeholderPattern = Pattern.compile(placeholderChar + "\\w+?" + placeholderChar);
  private static final List<ProxyChatPlaceHolder> placeholders = new LinkedList<>();

  public static Stream<ProxyChatPlaceHolder> getPlaceholderStream() {
    return placeholders.stream();
  }

  public static Stream<ProxyChatPlaceHolder> getApplicableStream(ProxyChatContext context) {
    return getPlaceholderStream().filter(placeholder -> placeholder.isContextApplicable(context));
  }

  public static Component processMessage(Component message, ProxyChatContext context) {
    final List<ProxyChatPlaceHolder> placeholders =
        getApplicableStream(context).toList();

    TextReplacementConfig config = TextReplacementConfig.builder()
            .match(placeholderPattern).replacement(match -> {
              String placeholderName = match.content().substring(1, match.content().length() -1);

              if(placeholderName.charAt(0) == '%') {
                return match;
              } else {
                Optional<ProxyChatPlaceHolder> placeholder =
                        placeholders.stream().filter(p -> p.matchesName(placeholderName)).findFirst();

                if(placeholder.isPresent()) {
                  match.content("");
                  match.append(placeholder.get().getReplacementComponent(placeholderName, context));
                }
              }

              return match;
            }).build();

    return message.replaceText(config);
  }

  public static String processMessage(String message, ProxyChatContext context) {
    final StringBuilder builder = new StringBuilder();
    final List<ProxyChatPlaceHolder> placeholders =
        getApplicableStream(context).collect(Collectors.toList());

    processMessageInternal(message, context, builder, placeholders);

    return builder.toString();
  }

  private static void processMessageInternal(
      String message,
      ProxyChatContext context,
      StringBuilder builder,
      List<ProxyChatPlaceHolder> placeholders) {
    boolean encounteredPlaceholder = false;
    StringBuilder placeholderName = null;

    for (char c : message.toCharArray()) {
      if (c == placeholderChar) {
        if (encounteredPlaceholder) {
          // We only need to do stuff if we are finding the end of the placeholder
          if (placeholderName == null) {
            // We just found the delimiter twice (this is an escape sequence). Add it to the buffer
            // once
            builder.append(placeholderChar);
          } else {
            // Render the placeholderName and delete the builder
            final String placeholderNameStr = placeholderName.toString();
            placeholderName = null;

            Optional<ProxyChatPlaceHolder> placeholder =
                placeholders.stream().filter(p -> p.matchesName(placeholderNameStr)).findFirst();

            if (placeholder.isPresent()) {
              // Apply the placeholder
              final String placeholderReplacement =
                  placeholder.get().getReplacement(placeholderNameStr, context);

              // Apply placeholders to that (note that appends any normal string parts)
              processMessageInternal(placeholderReplacement, context, builder, placeholders);
            } else {
              // Placeholder not found, let's add it to the output verbatim (with the delimiter
              // surrounding it)
              builder.append(placeholderChar).append(placeholderNameStr).append(placeholderChar);
            }
          }
        }

        // Toggle the state
        encounteredPlaceholder = !encounteredPlaceholder;
      } else {
        if (encounteredPlaceholder) {
          // We're parsing the name of the placeholder
          if (placeholderName == null) {
            // Create the instance for the builder if necessary
            placeholderName = new StringBuilder();
          }

          placeholderName.append(c);
        } else {
          // Just a normal char. Append it to the buffer
          builder.append(c);
        }
      }
    }

    if (encounteredPlaceholder) {
      builder.append(placeholderChar);

      if (placeholderName != null) {
        builder.append(placeholderName);
      }
    }
  }

  public static void registerPlaceholder(ProxyChatPlaceHolder... placeholder) {
    for (ProxyChatPlaceHolder p : placeholder) {
      registerPlaceholder(p);
    }
  }

  public static void registerPlaceholder(ProxyChatPlaceHolder placeholder) {
    if (placeholders.contains(placeholder))
      throw new IllegalArgumentException(
          "Placeholder " + placeholder.getName() + " has already been registered!");

    placeholders.add(placeholder);
  }

  public static void clear() {
    placeholders.clear();
  }

  private PlaceHolderManager() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
