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

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
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
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Configuration implements Config {
  protected static final ConfigParseOptions PARSE_OPTIONS =
      ConfigParseOptions.defaults().setAllowMissing(false).setSyntax(ConfigSyntax.CONF);
  protected static final ConfigRenderOptions RENDER_OPTIONS =
      ConfigRenderOptions.defaults().setOriginComments(false).setJson(false);
  protected static final String CONFIG_FILE_NAME = "config.conf";
  protected static final String OLD_CONFIG_FILE_NAME = "config.yml";
  protected static final String OLD_OLD_CONFIG_FILE_NAME = "config.old.yml";
  protected static final File CONFIG_FILE =
      new File(ProxyChat.getInstance().getConfigFolder(), CONFIG_FILE_NAME);
  protected static final File OLD_CONFIG_FILE =
      new File(CONFIG_FILE.getParentFile(), OLD_CONFIG_FILE_NAME);
  protected static final File OLD_OLD_CONFIG_FILE =
      new File(CONFIG_FILE.getParentFile(), OLD_OLD_CONFIG_FILE_NAME);

  @Getter(value = AccessLevel.PROTECTED, lazy = true)
  private static final String header = loadHeader();

  private static Configuration currentConfig;

  @Delegate protected Config config;

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
      @Cleanup
      BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(
                      Objects.requireNonNull(ProxyChat.getInstance().getClass().getClassLoader().getResourceAsStream(
                              CONFIG_FILE_NAME)),
                  StandardCharsets.UTF_8));
      String line;

      do {
        line = reader.readLine();

        if (line == null) throw new IOException("Unexpeted EOF while reading " + CONFIG_FILE_NAME);

        header.append(line).append('\n');
      } while (line.startsWith("#"));
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
      @Cleanup PrintWriter writer = new PrintWriter(CONFIG_FILE, StandardCharsets.UTF_8.name());
      String renderedConfig = config.root().render(RENDER_OPTIONS);
      renderedConfig = getHeader() + renderedConfig;

      writer.print(renderedConfig);
    } catch (FileNotFoundException | UnsupportedEncodingException e) {
      LoggerHelper.error("Something very unexpected happend! Please report this!", e);
    }
  }

  @SuppressWarnings("fallthrough")
  private void convertOldConfig() {
    if (OLD_CONFIG_FILE.exists()) {
      convertYAMLConfig();
    }

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

  private void convertYAMLConfig() {
    try {
      LoggerHelper.warning("Detected old YAML config. Trying to migrate to HOCON.");

      // Read old config
      ConfigurationNode oldConfig = YAMLConfigurationLoader.builder().setFile(OLD_CONFIG_FILE).build().load();
      ConfigurationNode section;

      // Migrate settigns
      section = oldConfig.getNode("AccountDataBase");
      final ImmutableMap<String, Object> accountDatabase =
          ImmutableMap.<String, Object>builder()
              .put("database", section.getNode("database").getString(""))
              .put("enabled", section.getNode("enabled").getBoolean())
              .put("ip", section.getNode("ip").getString(""))
              .put("password", section.getNode("password").getString(""))
              .put("port", section.getNode("port").getInt())
              .put("tablePrefix", section.getNode("tablePrefix").getString(""))
              .put("user", section.getNode("user").getString(""))
              .build();

      section = oldConfig.getNode("Formats");
      final ImmutableMap<String, Object> formats =
          ImmutableMap.<String, Object>builder()
              .put("alert", section.getNode("alert").getString(""))
              .put("chatLoggingConsole", section.getNode("chatLoggingConsole").getString(""))
              .put("chatLoggingFile", section.getNode("chatLoggingFile").getString(""))
              .put("globalChat", section.getNode("globalChat").getString(""))
              .put("joinMessage", section.getNode("joinMessage").getString(""))
              .put("leaveMessage", section.getNode("leaveMessage").getString(""))
              .put("localChat", section.getNode("localChat").getString(""))
              .put("localSpy", section.getNode("localSpy").getString(""))
              .put("messageSender", section.getNode("messageSender").getString(""))
              .put("messageTarget", section.getNode("messageTarget").getString(""))
              .put(
                  "motd",
                  String.join("\n",
                              oldConfig.getNode("Settings.Modules.MOTD.message").getList(Object::toString)))
              .put("serverSwitch", section.getString("serverSwitch"))
              .put("socialSpy", section.getString("socialSpy"))
              .put("staffChat", section.getString("staffChat"))
              .put(
                  "welcomeMessage",
                  String.join("\n",
                              oldConfig.getNode("Settings.Modules.WelcomeMessage.message").getList(Object::toString)))
              .build();

      final ConfigurationNode modulesSection =
          oldConfig.getNode("Settings.Modules");
      section = modulesSection.getNode("Alert");
      final ImmutableMap<String, Object> moduleAlert =
          ImmutableMap.<String, Object>builder()
              .put("aliases", section.getNode("aliases").getList(Object::toString))
              .put("enabled", section.getNode("enabled").getBoolean())
              .build();

      section = modulesSection.getNode("AntiAdvertising");
      final ImmutableMap<String, Object> moduleAntiAdvertising =
          ImmutableMap.<String, Object>builder()
              .put("enabled", section.getNode("enabled").getBoolean())
              .put("whitelisted", section.getNode("whitelisted").getList(Object::toString))
              .build();

      section = modulesSection.getNode("AntiDuplication");
      final ImmutableMap<String, Object> moduleAntiDuplication =
          ImmutableMap.<String, Object>builder()
              .put("checkPastMessages", section.getNode("checkPastMessages").getInt())
              .put("enabled", section.getNode("enabled").getBoolean())
              .build();

      section = modulesSection.getNode("AntiSwear");
      final ImmutableMap<String, Object> moduleAntiSwear =
          ImmutableMap.<String, Object>builder()
              .put("enabled", section.getNode("enabled").getBoolean())
              .put("freeMatching", section.getNode("freeMatching").getBoolean())
              .put("ignoreDuplicateLetters", section.getNode("ignoreDuplicateLetters").getBoolean())
              .put("ignoreSpaces", section.getNode("ignoreSpaces").getBoolean())
              .put("leetSpeak", section.getNode("leetSpeak").getBoolean())
              .put("replacement", section.getString("replacement"))
              .put("words", section.getNode("words").getList(Object::toString))
              .build();

      section = modulesSection.getNode("AutoBroadcast");
      final ImmutableMap<String, Object> moduleAutoBroadcast =
          ImmutableMap.<String, Object>builder()
              .put("enabled", section.getNode("enabled").getBoolean())
              .put("interval", section.getNode("interval").getInt() + "s")
              .put("messages", section.getNode("messages").getList(Object::toString))
              .put("random", section.getNode("random").getBoolean())
              .build();

      section = modulesSection.getNode("ChatLock");
      final ImmutableMap<String, Object> moduleChatLock =
          ImmutableMap.<String, Object>builder()
              .put("aliases", section.getNode("aliases").getList(Object::toString))
              .put("emptyLinesOnClear", section.getNode("emptyLinesOnClear").getInt())
              .put("enabled", section.getNode("enabled").getBoolean())
              .build();

      section = modulesSection.getNode("ChatLogging");
      final ImmutableMap<String, Object> moduleChatLogging =
          ImmutableMap.<String, Object>builder()
              .put("console", section.getNode("console").getBoolean())
              .put("enabled", section.getNode("enabled").getBoolean())
              .put("file", section.getNode("file").getBoolean())
              .put("filteredCommands", section.getNode("filteredCommands").getList(Object::toString))
              .put("logFile", section.getString("logFile"))
              .put("privateMessages", section.getNode("privateMessages").getBoolean())
              .build();

      section = modulesSection.getNode("ClearChat");
      final ImmutableMap<String, Object> moduleClearChat =
          ImmutableMap.<String, Object>builder()
              .put("aliases", section.getNode("aliases").getList(Object::toString))
              .put("emptyLinesOnClear", section.getNode("emptyLinesOnClear").getInt())
              .put("enabled", section.getNode("enabled").getBoolean())
              .build();

      section = modulesSection.getNode("GlobalChat");
      final ImmutableMap<String, Object> moduleGlobalChatServerList =
          ImmutableMap.<String, Object>builder()
              .put("enabled", section.getNode("serverList.enabled").getBoolean())
              .put("list", section.getNode("serverList.list").getList(Object::toString))
              .build();
      final ImmutableMap<String, Object> moduleGlobalChatSymbol =
          ImmutableMap.<String, Object>builder()
              .put("enabled", section.getNode("symbol.enabled").getBoolean())
              .put("symbol", section.getString("symbol.symbol"))
              .build();
      final ImmutableMap<String, Object> moduleGlobalChat =
          ImmutableMap.<String, Object>builder()
              .put("aliases", section.getNode("aliases").getList(Object::toString))
              .put("default", section.getNode("default").getBoolean())
              .put("enabled", section.getNode("enabled").getBoolean())
              .put("passToBackendServer", section.getNode("passToClientServer").getBoolean())
              .put("serverList", moduleGlobalChatServerList)
              .put("symbol", moduleGlobalChatSymbol)
              .build();

      section = modulesSection.getNode("Ignoring");
      final ImmutableMap<String, Object> moduleIgnoring =
          ImmutableMap.<String, Object>builder()
              .put("aliases", section.getNode("aliases").getList(Object::toString))
              .put("enabled", section.getNode("enabled").getBoolean())
              .build();

      section = modulesSection.getNode("JoinMessage");
      final ImmutableMap<String, Object> moduleJoinMessage =
          ImmutableMap.<String, Object>builder()
              .put("enabled", section.getNode("enabled").getBoolean())
              .build();

      section = modulesSection.getNode("LeaveMessage");
      final ImmutableMap<String, Object> moduleLeaveMessage =
          ImmutableMap.<String, Object>builder()
              .put("enabled", section.getNode("enabled").getBoolean())
              .build();

      section = modulesSection.getNode("LocalChat");
      final ImmutableMap<String, Object> moduleLocalChat =
          ImmutableMap.<String, Object>builder()
              .put("enabled", section.getNode("enabled").getBoolean())
              .put("passToBackendServer", section.getNode("passToClientServer").getBoolean())
              .build();

      section = modulesSection.getNode("MOTD");
      final ImmutableMap<String, Object> moduleMOTD =
          ImmutableMap.<String, Object>builder()
              .put("enabled", section.getNode("enabled").getBoolean())
              .build();

      section = modulesSection.getNode("Messenger");
      final ImmutableMap<String, Object> moduleMessengerAliases =
          ImmutableMap.<String, Object>builder()
              .put("message", section.getNode("aliases.message").getList(Object::toString))
              .put("msgtoggle", section.getNode("aliases.msgtoggle").getList(Object::toString))
              .put("reply", section.getNode("aliases.reply").getList(Object::toString))
              .build();
      final ImmutableMap<String, Object> moduleMessenger =
          ImmutableMap.<String, Object>builder()
              .put("aliases", moduleMessengerAliases)
              .put("enabled", section.getNode("enabled").getBoolean())
              .put("filterPrivateMessages", section.getNode("filterMessages").getBoolean())
              .build();

      section = modulesSection.getNode("Muting");
      final ImmutableMap<String, Object> moduleMutingAliases =
          ImmutableMap.<String, Object>builder()
              .put("mute", section.getNode("aliases.mute").getList(Object::toString))
              .put("tempmute", section.getNode("aliases.tempmute").getList(Object::toString))
              .put("unmute", section.getNode("aliases.unmute").getList(Object::toString))
              .build();
      final ImmutableMap<String, Object> moduleMuting =
          ImmutableMap.<String, Object>builder()
              .put("aliases", moduleMutingAliases)
              .put("disableWithOtherMutePlugins", section.getNode("disableWithOtherMutePlugins").getBoolean())
              .put("enabled", section.getNode("enabled").getBoolean())
              .build();

      section = modulesSection.getNode("ServerSwitchMessages");
      final ImmutableMap<String, Object> moduleServerSwitchMessages =
          ImmutableMap.<String, Object>builder()
              .put("enabled", section.getNode("enabled").getBoolean())
              .build();

      section = modulesSection.getNode("Spy");
      final ImmutableMap<String, Object> moduleSpyAliases =
          ImmutableMap.<String, Object>builder()
              .put("localspy", section.getNode("aliases.localspy").getList(Object::toString))
              .put("socialspy", section.getNode("aliases.socialspy").getList(Object::toString))
              .build();
      final ImmutableMap<String, Object> moduleSpy =
          ImmutableMap.<String, Object>builder()
              .put("aliases", moduleSpyAliases)
              .put("enabled", section.getNode("enabled").getBoolean())
              .build();

      section = modulesSection.getNode("StaffChat");
      final ImmutableMap<String, Object> moduleStaffChat =
          ImmutableMap.<String, Object>builder()
              .put("aliases", section.getNode("aliases").getList(Object::toString))
              .put("enabled", section.getNode("enabled").getBoolean())
              .put("passToBackendServer", section.getNode("passToClientServer").getBoolean())
              .build();

      section = modulesSection.getNode("Vanish");
      final ImmutableMap<String, Object> moduleVanish =
          ImmutableMap.<String, Object>builder()
              .put("aliases", section.getNode("aliases").getList(Object::toString))
              .put("enabled", section.getNode("enabled").getBoolean())
              .build();

      section = modulesSection.getNode("WelcomeMessage");
      final ImmutableMap<String, Object> moduleWelcomeMessage =
          ImmutableMap.<String, Object>builder()
              .put("enabled", section.getNode("enabled").getBoolean())
              .build();

      section = modulesSection.getNode("LocalTo");
      final ImmutableMap<String, Object> moduleLocalTo =
          ImmutableMap.<String, Object>builder()
              .put("aliases", section.getNode("aliases").getList(Object::toString))
              .put("enabled", section.getNode("enabled").getBoolean())
              .build();

      final ImmutableMap<String, Object> modules =
          ImmutableMap.<String, Object>builder()
              .put("Alert", moduleAlert)
              .put("AntiAdvertising", moduleAntiAdvertising)
              .put("AntiDuplication", moduleAntiDuplication)
              .put("AntiSwear", moduleAntiSwear)
              .put("AutoBroadcast", moduleAutoBroadcast)
              .put("ChatLock", moduleChatLock)
              .put("ChatLogging", moduleChatLogging)
              .put("ClearChat", moduleClearChat)
              .put("GlobalChat", moduleGlobalChat)
              .put("Ignoring", moduleIgnoring)
              .put("JoinMessage", moduleJoinMessage)
              .put("LeaveMessage", moduleLeaveMessage)
              .put("LocalChat", moduleLocalChat)
              .put("MOTD", moduleMOTD)
              .put("Messenger", moduleMessenger)
              .put("Muting", moduleMuting)
              .put("ServerSwitchMessages", moduleServerSwitchMessages)
              .put("Spy", moduleSpy)
              .put("StaffChat", moduleStaffChat)
              .put("Vanish", moduleVanish)
              .put("WelcomeMessage", moduleWelcomeMessage)
              .put("LocalTo", moduleLocalTo)
              .build();

      section = oldConfig.getNode("Settings.PermissionsManager");
      final ImmutableMap<String, Object> permissionsManager =
          ImmutableMap.<String, Object>builder()
              .put("defaultPrefix", section.getString("defaultPrefix"))
              .put("defaultSuffix", section.getString("defaultSuffix"))
              .build();

      section = oldConfig.getNode("Settings.ServerAlias");
//      ConfigurationNode finalSection = section;
//      final ImmutableMap<String, String> serverAlias =
//          section.getChildrenMap().keySet().stream()
//              .collect(MapUtils.immutableMapCollector(key -> key, finalSection.getNode(key).getString()));

      final ImmutableMap<String, Object> configMap =
          ImmutableMap.<String, Object>builder()
              .put("AccountDatabase", accountDatabase)
              .put("Formats", formats)
              .put("Modules", modules)
              .put("PrefixDefaults", permissionsManager)
//              .put("ServerAlias", serverAlias)
              .build();

      config =
          ConfigFactory.parseMap(configMap)
              .withFallback(config.withoutPath("ServerAlias"))
              .resolve()
              .withValue("Version", ConfigValueFactory.fromAnyRef("11.3"));

      // Rename old file
      Files.move(OLD_CONFIG_FILE, OLD_OLD_CONFIG_FILE);

      LoggerHelper.info("Old config file has been renamed to config.old.yml.");

      // Done
      LoggerHelper.info("Migration successful!");
    } catch (Exception e) {
      LoggerHelper.error("There has been an error while migrating the old YAML config file!", e);
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
}
