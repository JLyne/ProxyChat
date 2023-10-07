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

public class Configuration implements Config {
  protected static final ConfigParseOptions PARSE_OPTIONS =
      ConfigParseOptions.defaults().setAllowMissing(false).setSyntax(ConfigSyntax.CONF);
  protected static final ConfigRenderOptions RENDER_OPTIONS =
      ConfigRenderOptions.defaults().setOriginComments(false).setJson(false);
  protected static final String CONFIG_FILE_NAME = "config.conf";
  protected static final File CONFIG_FILE =
      new File(ProxyChat.getInstance().getConfigFolder(), CONFIG_FILE_NAME);

  private static final java.util.concurrent.atomic.AtomicReference<Object> header = new java.util.concurrent.atomic.AtomicReference<Object>();


  private static Configuration currentConfig;
  protected Config config;

  /**
   * Creates and loads the config. Also saves it so that all missing values exist!<br>
   * Also set currentConfig to this config.
   *
   * @return a configuration object, loaded from the config file.
   */
  public static Configuration load() {
    Configuration config = new Configuration();
    config.loadConfig();

    currentConfig = config;

    return currentConfig;
  }

  public static Configuration get() {
    return currentConfig;
  }

  private static String loadHeader() {
    StringBuilder header = new StringBuilder();

    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(ProxyChat.getInstance().getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)), StandardCharsets.UTF_8));
      try {
        String line;
        do {
          line = reader.readLine();
          if (line == null) throw new IOException("Unexpeted EOF while reading " + CONFIG_FILE_NAME);
          header.append(line).append('\n');
        } while (line.startsWith("#"));
      } finally {
        if (reader != null) {
          reader.close();
        }
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
    // Reapply default config. By default this does nothing but it can fix the missing config
    // settings in some cases
    config = config.withFallback(strippedDefautConfig);
    copyComments(defaultConfig);

    if (saveConfig) saveConfig();
  }

  protected void saveConfig() {
    try {
      PrintWriter writer = new PrintWriter(CONFIG_FILE, StandardCharsets.UTF_8.name());
      try {
        String renderedConfig = config.root().render(RENDER_OPTIONS);
        renderedConfig = getHeader() + renderedConfig;
        writer.print(renderedConfig);
      } finally {
        if (writer != null) {
          writer.close();
        }
      }
    } catch (FileNotFoundException | UnsupportedEncodingException e) {
      LoggerHelper.error("Something very unexpected happend! Please report this!", e);
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
            langFile.delete();
          }
        }
      case "11.2":
        LoggerHelper.info("Performing config migration 11.2 -> 11.3 ...");

        // Remove config section "Modules.TabCompletion"
        config = config.withoutPath("Modules.TabCompletion");
      case "11.3":
        LoggerHelper.info("Performing config migration 11.3 -> 11.4 ...");

        final Config gloabalServerList = config.getConfig("Modules.GlobalChat.serverList");

        // Copy over server list from Global to AutoBroadcast if it is enabled
        if (gloabalServerList.getBoolean("enabled")) {
          config = config.withValue("Modules.AutoBroadcast.serverList", gloabalServerList.root());
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

        // Convert autobroadcast settings to a list
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

        //Convert autobroadcast messages from legacy to minimessage syntax
        for (ConfigValue broadcast : existingBroadcasts) {
          if(!(broadcast instanceof ConfigObject)) {
            LoggerHelper.warning("Skipping invalid autobroadcast config");
            continue;
          }

          Map<String, Object> value = ((ConfigObject) broadcast).unwrapped();

          if(!(value.get("messages") instanceof List)) {
            LoggerHelper.warning("Skipping invalid autobroadcast config");
            return;
          }

          value.put("messages", ((List<String>) value.get("messages")).stream()
                  .map(message -> ComponentUtil.miniMessage.serialize(
                          ComponentUtil.legacySerializer.deserialize(message)))
                  .collect(Collectors.toList()));

          broadcasts.add(ConfigValueFactory.fromAnyRef(value));
        }

        // Convert autobroadcast settings to a list
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
        // Unknow Version or old version
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
              .collect(Collectors.toList()));

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

  protected Configuration() {
  }

  protected static String getHeader() {
    Object value = Configuration.header.get();
    if (value == null) {
      synchronized (Configuration.header) {
        value = Configuration.header.get();
        if (value == null) {
          final String actualValue = loadHeader();
          value = actualValue == null ? Configuration.header : actualValue;
          Configuration.header.set(value);
        }
      }
    }
    return (String) (value == Configuration.header ? null : value);
  }

  public com.typesafe.config.ConfigObject root() {
    return this.config.root();
  }

  public com.typesafe.config.ConfigOrigin origin() {
    return this.config.origin();
  }

  public com.typesafe.config.Config withFallback(final com.typesafe.config.ConfigMergeable arg0) {
    return this.config.withFallback(arg0);
  }

  public com.typesafe.config.Config resolve() {
    return this.config.resolve();
  }

  public com.typesafe.config.Config resolve(final com.typesafe.config.ConfigResolveOptions arg0) {
    return this.config.resolve(arg0);
  }

  public boolean isResolved() {
    return this.config.isResolved();
  }

  public com.typesafe.config.Config resolveWith(final com.typesafe.config.Config arg0) {
    return this.config.resolveWith(arg0);
  }

  public com.typesafe.config.Config resolveWith(final com.typesafe.config.Config arg0, final com.typesafe.config.ConfigResolveOptions arg1) {
    return this.config.resolveWith(arg0, arg1);
  }

  public void checkValid(final com.typesafe.config.Config arg0, final java.lang.String... arg1) {
    this.config.checkValid(arg0, arg1);
  }

  public boolean hasPath(final java.lang.String arg0) {
    return this.config.hasPath(arg0);
  }

  public boolean hasPathOrNull(final java.lang.String arg0) {
    return this.config.hasPathOrNull(arg0);
  }

  public boolean isEmpty() {
    return this.config.isEmpty();
  }

  public java.util.Set<java.util.Map.Entry<java.lang.String, com.typesafe.config.ConfigValue>> entrySet() {
    return this.config.entrySet();
  }

  public boolean getIsNull(final java.lang.String arg0) {
    return this.config.getIsNull(arg0);
  }

  public boolean getBoolean(final java.lang.String arg0) {
    return this.config.getBoolean(arg0);
  }

  public java.lang.Number getNumber(final java.lang.String arg0) {
    return this.config.getNumber(arg0);
  }

  public int getInt(final java.lang.String arg0) {
    return this.config.getInt(arg0);
  }

  public long getLong(final java.lang.String arg0) {
    return this.config.getLong(arg0);
  }

  public double getDouble(final java.lang.String arg0) {
    return this.config.getDouble(arg0);
  }

  public java.lang.String getString(final java.lang.String arg0) {
    return this.config.getString(arg0);
  }

  public <T extends java.lang.Enum<T>> T getEnum(final java.lang.Class<T> arg0, final java.lang.String arg1) {
    return this.config.<T>getEnum(arg0, arg1);
  }

  public com.typesafe.config.ConfigObject getObject(final java.lang.String arg0) {
    return this.config.getObject(arg0);
  }

  public com.typesafe.config.Config getConfig(final java.lang.String arg0) {
    return this.config.getConfig(arg0);
  }

  public java.lang.Object getAnyRef(final java.lang.String arg0) {
    return this.config.getAnyRef(arg0);
  }

  public com.typesafe.config.ConfigValue getValue(final java.lang.String arg0) {
    return this.config.getValue(arg0);
  }

  public java.lang.Long getBytes(final java.lang.String arg0) {
    return this.config.getBytes(arg0);
  }

  public com.typesafe.config.ConfigMemorySize getMemorySize(final java.lang.String arg0) {
    return this.config.getMemorySize(arg0);
  }

  @Deprecated
  public java.lang.Long getMilliseconds(final java.lang.String arg0) {
    return this.config.getMilliseconds(arg0);
  }

  @Deprecated
  public java.lang.Long getNanoseconds(final java.lang.String arg0) {
    return this.config.getNanoseconds(arg0);
  }

  public long getDuration(final java.lang.String arg0, final java.util.concurrent.TimeUnit arg1) {
    return this.config.getDuration(arg0, arg1);
  }

  public java.time.Duration getDuration(final java.lang.String arg0) {
    return this.config.getDuration(arg0);
  }

  public java.time.Period getPeriod(final java.lang.String arg0) {
    return this.config.getPeriod(arg0);
  }

  public java.time.temporal.TemporalAmount getTemporal(final java.lang.String arg0) {
    return this.config.getTemporal(arg0);
  }

  public com.typesafe.config.ConfigList getList(final java.lang.String arg0) {
    return this.config.getList(arg0);
  }

  public java.util.List<java.lang.Boolean> getBooleanList(final java.lang.String arg0) {
    return this.config.getBooleanList(arg0);
  }

  public java.util.List<java.lang.Number> getNumberList(final java.lang.String arg0) {
    return this.config.getNumberList(arg0);
  }

  public java.util.List<java.lang.Integer> getIntList(final java.lang.String arg0) {
    return this.config.getIntList(arg0);
  }

  public java.util.List<java.lang.Long> getLongList(final java.lang.String arg0) {
    return this.config.getLongList(arg0);
  }

  public java.util.List<java.lang.Double> getDoubleList(final java.lang.String arg0) {
    return this.config.getDoubleList(arg0);
  }

  public java.util.List<java.lang.String> getStringList(final java.lang.String arg0) {
    return this.config.getStringList(arg0);
  }

  public <T extends java.lang.Enum<T>> java.util.List<T> getEnumList(final java.lang.Class<T> arg0, final java.lang.String arg1) {
    return this.config.<T>getEnumList(arg0, arg1);
  }

  public java.util.List<? extends com.typesafe.config.ConfigObject> getObjectList(final java.lang.String arg0) {
    return this.config.getObjectList(arg0);
  }

  public java.util.List<? extends com.typesafe.config.Config> getConfigList(final java.lang.String arg0) {
    return this.config.getConfigList(arg0);
  }

  public java.util.List<?> getAnyRefList(final java.lang.String arg0) {
    return this.config.getAnyRefList(arg0);
  }

  public java.util.List<java.lang.Long> getBytesList(final java.lang.String arg0) {
    return this.config.getBytesList(arg0);
  }

  public java.util.List<com.typesafe.config.ConfigMemorySize> getMemorySizeList(final java.lang.String arg0) {
    return this.config.getMemorySizeList(arg0);
  }

  @Deprecated
  public java.util.List<java.lang.Long> getMillisecondsList(final java.lang.String arg0) {
    return this.config.getMillisecondsList(arg0);
  }

  @Deprecated
  public java.util.List<java.lang.Long> getNanosecondsList(final java.lang.String arg0) {
    return this.config.getNanosecondsList(arg0);
  }

  public java.util.List<java.lang.Long> getDurationList(final java.lang.String arg0, final java.util.concurrent.TimeUnit arg1) {
    return this.config.getDurationList(arg0, arg1);
  }

  public java.util.List<java.time.Duration> getDurationList(final java.lang.String arg0) {
    return this.config.getDurationList(arg0);
  }

  public com.typesafe.config.Config withOnlyPath(final java.lang.String arg0) {
    return this.config.withOnlyPath(arg0);
  }

  public com.typesafe.config.Config withoutPath(final java.lang.String arg0) {
    return this.config.withoutPath(arg0);
  }

  public com.typesafe.config.Config atPath(final java.lang.String arg0) {
    return this.config.atPath(arg0);
  }

  public com.typesafe.config.Config atKey(final java.lang.String arg0) {
    return this.config.atKey(arg0);
  }

  public com.typesafe.config.Config withValue(final java.lang.String arg0, final com.typesafe.config.ConfigValue arg1) {
    return this.config.withValue(arg0, arg1);
  }
}
