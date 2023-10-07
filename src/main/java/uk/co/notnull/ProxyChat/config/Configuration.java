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

package uk.co.notnull.ProxyChat.config;

import com.typesafe.config.*;
import uk.co.notnull.ProxyChat.ProxyChat;
import uk.co.notnull.ProxyChat.api.ProxyChatApi;
import uk.co.notnull.ProxyChat.util.ComponentUtil;
import uk.co.notnull.ProxyChat.util.LoggerHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Configuration {
  protected static final ConfigParseOptions PARSE_OPTIONS =
      ConfigParseOptions.defaults().setAllowMissing(false).setSyntax(ConfigSyntax.CONF);
  protected static final ConfigRenderOptions RENDER_OPTIONS =
      ConfigRenderOptions.defaults().setOriginComments(false).setJson(false);
  protected static final String CONFIG_FILE_NAME = "config.conf";
  protected static final File CONFIG_FILE =
      new File(ProxyChat.getInstance().getConfigFolder(), CONFIG_FILE_NAME);

  private static final java.util.concurrent.atomic.AtomicReference<Object> header = new java.util.concurrent.atomic.AtomicReference<>();

  private static Configuration instance;
  protected Config config;

  protected Configuration() {
  }

  /**
   * Creates and loads the config. Also saves it so that all missing values exist!<br>
   * Also set currentConfig to this config.
   */
  public static void load() {
    Configuration config = new Configuration();
    config.loadConfig();

    instance = config;
  }

  public static Config get() {
    return instance.getConfig();
  }

  private static String loadHeader() {
    StringBuilder header = new StringBuilder();

    try {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
              ProxyChat.getInstance().getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)),
                                                                            StandardCharsets.UTF_8))) {
        String line;
        do {
          line = reader.readLine();
          if (line == null) throw new IOException("Unexpected EOF while reading " + CONFIG_FILE_NAME);
          header.append(line).append('\n');
        } while (line.startsWith("#"));
      }
    } catch (IOException e) {
      LoggerHelper.error("Error loading file header", e);
    }

    return header.toString();
  }

  private static Collection<String> getPaths(ConfigValue config) {
    if (config instanceof ConfigObject) {
      return new ArrayList<>(((ConfigObject) config).keySet());
    } else {
      return Collections.emptyList();
    }
  }

  private static List<String> getComment(Config config, String path) {
    return config.hasPath(path) ? getComment(config.getValue(path)) : Collections.emptyList();
  }

  private static List<String> getComment(ConfigValue config) {
    return config.origin().comments();
  }

  protected void loadConfig() {
    boolean saveConfig = true;
    final Config defaultConfig =
        ConfigFactory.parseReader(
            new InputStreamReader(
                    Objects.requireNonNull(
                            ProxyChat.getInstance().getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)),
                    StandardCharsets.UTF_8),
            PARSE_OPTIONS);
    final Config strippedDefautConfig = defaultConfig.withoutPath("ServerAlias");

    if (CONFIG_FILE.exists()) {
      try {
        Config fileConfig = ConfigFactory.parseFile(CONFIG_FILE, PARSE_OPTIONS);

        config = fileConfig.withFallback(strippedDefautConfig);
      } catch (ConfigException e) {
        LoggerHelper.error(
            "====================================================================================================");
        LoggerHelper.error("Error while reading config:\n" + e.getLocalizedMessage());
        LoggerHelper.error(
            "The plugin will run with the default config (but the config file has not been changed)!");
        LoggerHelper.error(
            "After you fixed the issue, either restart the server or run `/proxychat reload`.");
        LoggerHelper.error(
            "====================================================================================================");

        saveConfig = false;
        config = defaultConfig;
      }
    } else {
      config = defaultConfig;
    }

    config = config.resolve();

    convertOldConfig();
    // Reapply default config. By default, this does nothing, but it can fix the missing config
    // settings in some cases
    config = config.withFallback(strippedDefautConfig);
    copyComments(defaultConfig);

    if (saveConfig) saveConfig();
  }

  protected void saveConfig() {
    try {
      //noinspection CharsetObjectCanBeUsed
      try (PrintWriter writer = new PrintWriter(CONFIG_FILE, StandardCharsets.UTF_8.name())) {
        String renderedConfig = config.root().render(RENDER_OPTIONS);
        renderedConfig = getHeader() + renderedConfig;
        writer.print(renderedConfig);
      }
    } catch (FileNotFoundException | UnsupportedEncodingException e) {
      LoggerHelper.error("Something very unexpected happened! Please report this!", e);
    }
  }

  @SuppressWarnings("fallthrough")
  private void convertOldConfig() {
    switch (String.format(Locale.ROOT, "%.1f", config.getDouble("Version"))) {
      case "11.0":
        LoggerHelper.info("Performing config migration 11.0 -> 11.1 ...");

        // Rename "passToClientServer" to "passToBackendServer"
        for (String basePath :
            new String[] {"Modules.GlobalChat", "Modules.LocalChat", "Modules.StaffChat"}) {
          final String newPath = basePath + ".passToBackendServer";
          final String oldPath = basePath + ".passToClientServer";

          // Remove old path first to reduce the amount of data that needs to be copied
          config = config.withoutPath(oldPath).withValue(newPath, config.getValue(oldPath));
        }
      case "11.1":
        LoggerHelper.info("Performing config migration 11.1 -> 11.2 ...");

        // Delete old language files
        final File langDir = ProxyChat.getInstance().getLangFolder();
        File langFile;

        for (String lang :
            new String[] {"de_DE", "en_US", "fr_FR", "hu_HU", "nl_NL", "pl_PL", "ru_RU", "zh_CN"}) {
          langFile = new File(langDir, lang + ".lang");

          if (langFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            langFile.delete();
          }
        }
      case "11.2":
        LoggerHelper.info("Performing config migration 11.2 -> 11.3 ...");

        // Remove config section "Modules.TabCompletion"
        config = config.withoutPath("Modules.TabCompletion");
      case "11.3":
        LoggerHelper.info("Performing config migration 11.3 -> 11.4 ...");

        final Config globalServerList = config.getConfig("Modules.GlobalChat.serverList");

        // Copy over server list from Global to AutoBroadcast if it is enabled
        if (globalServerList.getBoolean("enabled")) {
          config = config.withValue("Modules.AutoBroadcast.serverList", globalServerList.root());
        }
      case "11.4":
        LoggerHelper.info("Performing config migration 11.4 -> 11.5 ...");

        // Move the server lists section one layer down
        config =
            config.withValue(
                "Modules.MulticastChat.serverLists",
                config.getValue("Modules.MulticastChat.serverLists.lists"));
      case "11.5":
        LoggerHelper.info("Performing config migration 11.5 -> 11.6 ...");

        // Rename PrefixDefaults to PrefixSuffixSettings
        config =
            config
                .withoutPath("PrefixDefaults")
                .withValue("PrefixSuffixSettings", config.getValue("PrefixDefaults"));

      case "11.6":
        List<ConfigValue> broadcasts = new ArrayList<>();
        ConfigValue enabled = config.getValue("Modules.AutoBroadcast.enabled");
        ConfigValue existing = config.getObject("Modules.AutoBroadcast").withoutKey("broadcasts").withoutKey("enabled");

        broadcasts.add(existing);

        // Convert auto-broadcast settings to a list
        config =
            config
                .withoutPath("Modules.AutoBroadcast")
                .withValue("Modules.AutoBroadcast.broadcasts", ConfigValueFactory.fromIterable(broadcasts))
                .withValue("Modules.AutoBroadcast.enabled", enabled);

      case "11.7":
        Map<String, List<String>> emotes = new HashMap<>();

        if(config.hasPath("Modules.Emotes.emoteNames")) {
          List<String> emoteNames = config.getStringList("Modules.Emotes.emoteNames");
          String prefix = null;

          if(config.hasPath("Modules.Emotes.prefix")) {
            prefix = config.getString("Modules.Emotes.prefix").toLowerCase();
          }

          char emoteCharacter = '\ue110';
          AtomicInteger index = new AtomicInteger();
          String finalPrefix = prefix;

          emoteNames.forEach(emote -> {
            String emoteName = emote.toLowerCase();
            String character = new String(Character.toChars(emoteCharacter + index.getAndIncrement()));

            List<String> names = new ArrayList<>();
            names.add(emoteName);

            if(finalPrefix != null) {
              names.add(finalPrefix + emoteName);
            }

            emotes.put(character, names);
          });

          config = config.withoutPath("Modules.Emotes.prefix").withoutPath("Modules.Emotes.emoteNames")
                  .withValue("Modules.Emotes.emotes.General", ConfigValueFactory.fromAnyRef(emotes));
        }

      case "11.8":
        List<String> formats = List.of(
                "alert", "chatLoggingConsole", "chatLoggingFile",
                "globalChat", "joinMessage", "leaveMessage",
                "localChat", "localSpy", "messageSender",
                "messageTarget", "motd", "serverSwitch",
                "socialSpy", "staffChat", "welcomeMessage");

        //Convert formats from legacy to minimessage syntax
        for (String format : formats) {
          String newFormat = ComponentUtil.miniMessage.serialize(
                  ComponentUtil.legacySerializer.deserialize(config.getString("Formats." + format)));

          config = config.withValue("Formats." + format, ConfigValueFactory.fromAnyRef(newFormat));
        }

        broadcasts = new ArrayList<>();
        ConfigList existingBroadcasts = config.getList("Modules.AutoBroadcast.broadcasts");

        //Convert auto-broadcast messages from legacy to minimessage syntax
        for (ConfigValue broadcast : existingBroadcasts) {
          if(!(broadcast instanceof ConfigObject)) {
            LoggerHelper.warning("Skipping invalid auto-broadcast config");
            continue;
          }

          Map<String, Object> value = ((ConfigObject) broadcast).unwrapped();

          if(!(value.get("messages") instanceof List)) {
            LoggerHelper.warning("Skipping invalid auto-broadcast config");
            return;
          }

          //noinspection unchecked
          value.put("messages", ((List<String>) value.get("messages")).stream()
                  .map(message -> ComponentUtil.miniMessage.serialize(
                          ComponentUtil.legacySerializer.deserialize(message)))
                  .collect(Collectors.toList()));

          broadcasts.add(ConfigValueFactory.fromAnyRef(value));
        }

        // Convert auto-broadcast settings to a list
        config =
            config
                .withoutPath("Modules.AutoBroadcast.broadcasts")
                .withValue("Modules.AutoBroadcast.broadcasts", ConfigValueFactory.fromIterable(broadcasts));

      case "11.9":
        LoggerHelper.info("Performing config migration 11.9 -> 12.0...");

        config = config.withoutPath("Modules.Emotes")
                .withValue("Modules.Emoji.enabled", config.getValue("Modules.Emotes.enabled"))
                .withValue("Modules.Emoji.custom-emoji", config.getValue("Modules.Emotes.emotes"));

      case "12.0":
        LoggerHelper.info("Performing config migration 12.0 -> 12.1...");

        config = config
                .withValue("PrefixSuffixSettings.defaultPrefix",
                           ConfigValueFactory.fromAnyRef(
                                   ComponentUtil.miniMessage.serialize(
                                           ComponentUtil.legacySerializer.deserialize(
                                                   config.getString("PrefixSuffixSettings.defaultPrefix")))))
                .withValue("PrefixSuffixSettings.defaultSuffix",
                           ConfigValueFactory.fromAnyRef(
                                   ComponentUtil.miniMessage.serialize(
                                           ComponentUtil.legacySerializer.deserialize(
                                                   config.getString("PrefixSuffixSettings.defaultSuffix")))));

      default:
        // Unknown Version or old version
        // -> Update version
        config =
            config.withValue(
                "Version", ConfigValueFactory.fromAnyRef(ProxyChatApi.CONFIG_VERSION));

      case "12.1":
        // Up to date
        // -> No action needed
    }
  }

  private void copyComments(Config defaultConfig) {
    final Queue<String> paths = new LinkedList<>(getPaths(config.root()));

    while (!paths.isEmpty()) {
      final String path = paths.poll();
      final ConfigValue currentConfig = config.getValue(path);

      // Add new paths to path list
      paths.addAll(
          getPaths(currentConfig).stream()
              .map(newPath -> path + '.' + newPath)
              .toList());

      // If the current value has a comment we will not override it
      if (!getComment(currentConfig).isEmpty()) continue;

      final List<String> comments = getComment(defaultConfig, path);

      // If the default config has no comments we can't set any
      if (comments.isEmpty()) continue;

      // Set comment
      config =
          config.withValue(
              path, currentConfig.withOrigin(currentConfig.origin().withComments(comments)));
    }
  }

  protected static String getHeader() {
    Object value = Configuration.header.get();
    if (value == null) {
      synchronized (Configuration.header) {
        value = Configuration.header.get();
        if (value == null) {
          value = loadHeader();
          Configuration.header.set(value);
        }
      }
    }
    return (String) (value == Configuration.header ? null : value);
  }

  public Config getConfig() {
    return this.config;
  }
}
