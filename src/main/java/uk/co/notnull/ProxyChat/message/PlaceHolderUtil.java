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

import com.typesafe.config.Config;
import uk.co.notnull.ProxyChat.ProxyChat;
import uk.co.notnull.ProxyChat.api.placeholder.ProxyChatContext;
import uk.co.notnull.ProxyChat.api.placeholder.PlaceHolderManager;
import uk.co.notnull.ProxyChat.config.Configuration;
import dev.aura.lib.messagestranslator.MessagesTranslator;
import dev.aura.lib.messagestranslator.PluginMessagesTranslator;
import java.io.File;
import net.kyori.adventure.text.Component;
import uk.co.notnull.ProxyChat.util.ComponentUtil;

public final class PlaceHolderUtil {
  private static final String FORMATS = "Formats";
  private static final String LANGUAGE = "Language";
  private static Config formatsBase;
  private static MessagesTranslator messageBase;

  private static final char placeholderChar = PlaceHolderManager.placeholderChar;
  private static final String placeholderString = String.valueOf(placeholderChar);

  public static void clearConfigSections() {
    formatsBase = null;
    messageBase = null;
  }

  public static void loadConfigSections() {
    loadFormatsBase();
    loadMessageBase();
  }

  public static void loadFormatsBase() {
    formatsBase = Configuration.get().getConfig(FORMATS);
  }

  public static void loadMessageBase() {
    File dir = ProxyChat.getInstance().getLangFolder();
    String language = Configuration.get().getString(LANGUAGE);

    messageBase =
        new PluginMessagesTranslator(dir, language, ProxyChat.getInstance(), ProxyChat.ID);
  }

  public static Component getFormat(Format format) {
    try {
      if (formatsBase == null) {
        loadFormatsBase();
      }

      return ComponentUtil.miniMessage.deserialize(formatsBase.getString(format.getStringPath()));
    } catch (RuntimeException e) {
      return ComponentUtil.miniMessage.deserialize(format.getStringPath());
    }
  }

  public static String getFormatRaw(Format format) {
    try {
      if (formatsBase == null) {
        loadFormatsBase();
      }

      return formatsBase.getString(format.getStringPath());
    } catch (RuntimeException e) {
      return format.getStringPath();
    }
  }

  public static Component getMessage(Messages message) {
    try {
      if (messageBase == null) {
        loadMessageBase();
      }

      return ComponentUtil.miniMessage.deserialize(messageBase.translateWithFallback(message));
    } catch (RuntimeException e) {
      return ComponentUtil.miniMessage.deserialize(message.getStringPath());
    }
  }

  public static String getMessageRaw(Messages message) {
    try {
      if (messageBase == null) {
        loadMessageBase();
      }

      return messageBase.translateWithFallback(message);
    } catch (RuntimeException e) {
      return message.getStringPath();
    }
  }

  public static Component getFullFormatMessage(Format format, ProxyChatContext context) {
    return formatMessage(getFormat(format), context);
  }

  public static String getFullFormatMessageRaw(Format format, ProxyChatContext context) {
    return formatMessageRaw(getFormatRaw(format), context);
  }

  public static Component getFullMessage(Messages message) {
    return formatMessage(getMessage(message), new ProxyChatContext());
  }

  public static String getFullMessageRaw(Messages message) {
    return formatMessageRaw(getMessageRaw(message), new ProxyChatContext());
  }

  public static Component getFullMessage(Messages message, ProxyChatContext context) {
    return formatMessage(getMessage(message), context);
  }

   public static String getFullMessageRaw(Messages message, ProxyChatContext context) {
    return formatMessageRaw(getMessageRaw(message), context);
  }

  public static Component formatMessage(Component message, ProxyChatContext context) {
    return PlaceHolderManager.processMessage(message, context);
  }

  public static String formatMessageRaw(String message, ProxyChatContext context) {
    return PlaceHolderManager.processMessage(message, context);
  }

  public static String escapePlaceholders(String message) {
    return message.replace(placeholderString, placeholderString + placeholderString);
  }

  private PlaceHolderUtil() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
