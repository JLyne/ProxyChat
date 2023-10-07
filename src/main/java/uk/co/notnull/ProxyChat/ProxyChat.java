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

package uk.co.notnull.ProxyChat;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import uk.co.notnull.ProxyChat.account.AccountFileStorage;
import uk.co.notnull.ProxyChat.account.AccountSQLStorage;
import uk.co.notnull.ProxyChat.account.ProxyChatAccountManager;
import uk.co.notnull.ProxyChat.api.ProxyChatApi;
import uk.co.notnull.ProxyChat.api.account.AccountManager;
import uk.co.notnull.ProxyChat.api.enums.ChannelType;
import uk.co.notnull.ProxyChat.api.hook.HookManager;
import uk.co.notnull.ProxyChat.api.module.ModuleManager;
import uk.co.notnull.ProxyChat.api.placeholder.ProxyChatContext;
import uk.co.notnull.ProxyChat.api.placeholder.InvalidContextError;
import uk.co.notnull.ProxyChat.api.placeholder.PlaceHolderManager;
import uk.co.notnull.ProxyChat.api.utils.ProxyChatInstaceHolder;
import uk.co.notnull.ProxyChat.command.ProxyChatCommand;
import uk.co.notnull.ProxyChat.config.Configuration;
import uk.co.notnull.ProxyChat.hook.DefaultHook;
import uk.co.notnull.ProxyChat.listener.MutingListener;
import uk.co.notnull.ProxyChat.listener.ProxyChatEventsListener;
import uk.co.notnull.ProxyChat.listener.ChannelTypeCorrectorListener;
import uk.co.notnull.ProxyChat.message.MessagesService;
import uk.co.notnull.ProxyChat.message.PlaceHolderUtil;
import uk.co.notnull.ProxyChat.message.PlaceHolders;
import uk.co.notnull.ProxyChat.module.ProxyChatModuleManager;
import uk.co.notnull.ProxyChat.util.LoggerHelper;
import uk.co.notnull.ProxyChat.util.MapUtils;
import uk.co.notnull.ProxyChat.util.ServerNameUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

public class ProxyChat implements ProxyChatApi {
  private static final String storedDataHookName = "storedData";
  private static final String defaultHookName = "default";
  @VisibleForTesting
  static ProxyChat instance;

  @Inject
  @DataDirectory
  private Path configDir;
  private File langDir;

  private final ProxyServer proxy;
  private final Logger logger;

  private ProxyChatAccountManager proxyChatAccountManager;
  private ChannelTypeCorrectorListener channelTypeCorrectorListener;
  private ProxyChatEventsListener proxyChatEventsListener;
  private MutingListener mutingListener;
  private ProxyDiscordHandler proxyDiscordHandler;
  private SuperVanishBridgeHandler superVanishBridgeHandler;

  @Inject
  public ProxyChat(ProxyServer proxy, Logger logger) {
    this.proxy = proxy;
    this.logger = logger;
  }

  /** For unit tests only! */
  protected ProxyChat(ProxyServer proxy, Logger logger, PluginDescription description) {
    this.proxy = proxy;
    this.logger = logger;
  }

  public void onLoad() {
    setInstance(this);
    ProxyChatInstaceHolder.setInstance(instance);
  }

  @Subscribe
  public void onProxyInitialized(ProxyInitializeEvent event) {
    onLoad();

    if(proxy.getPluginManager().isLoaded("proxydiscord")) {
      proxyDiscordHandler = new ProxyDiscordHandler(this);
    }
    
    if(proxy.getPluginManager().isLoaded("supervanishbridge")) {
      superVanishBridgeHandler = new SuperVanishBridgeHandler(this);
    }

    onEnable(true);
  }

  @Subscribe
  public void onProxyReload(ProxyReloadEvent event) {
    onDisable();
    onEnable(false);
  }

  public void onEnable(boolean prinLoadScreen) {
    Configuration.load();
    PlaceHolderUtil.loadConfigSections();

    PlaceHolders.registerPlaceHolders();

    final Config accountDatabase = Configuration.get().getConfig("AccountDatabase");
    final Config databaseCredentials = accountDatabase.getConfig("credentials");
    final Config connectionProperties = accountDatabase.getConfig("properties");
    final ImmutableMap<String, String> connectionPropertiesMap =
        connectionProperties.entrySet().stream()
            .collect(
                MapUtils.immutableMapCollector(
                    Map.Entry::getKey, entry -> entry.getValue().unwrapped().toString()));

    if (accountDatabase.getBoolean("enabled")) {
      try {
        AccountManager.setAccountStorage(
            new AccountSQLStorage(
                databaseCredentials.getString("ip"),
                databaseCredentials.getInt("port"),
                databaseCredentials.getString("database"),
                databaseCredentials.getString("user"),
                databaseCredentials.getString("password"),
                databaseCredentials.getString("tablePrefix"),
                connectionPropertiesMap));
      } catch (SQLException e) {
        LoggerHelper.error("Could not connect to specified database. Using file storage", e);

        AccountManager.setAccountStorage(new AccountFileStorage());
      }
    } else {
      AccountManager.setAccountStorage(new AccountFileStorage());
    }

    ProxyChatCommand proxyChatCommand = new ProxyChatCommand();
    proxyChatAccountManager = new ProxyChatAccountManager();
    channelTypeCorrectorListener = new ChannelTypeCorrectorListener();
    mutingListener = new MutingListener();
    proxyChatEventsListener = new ProxyChatEventsListener();

    proxyChatCommand.register();
    proxy.getEventManager().register(this, proxyChatAccountManager);
    proxy.getEventManager().register(this, mutingListener);
    proxy.getEventManager().register(this, channelTypeCorrectorListener);
    proxy.getEventManager().register(this, proxyChatEventsListener);

    Config prefixDefaults = Configuration.get().getConfig("PrefixSuffixSettings");

    ProxyChatModuleManager.registerPluginModules();
    ModuleManager.enableModules();
    HookManager.addHook(
        defaultHookName,
        new DefaultHook(
            prefixDefaults.getString("defaultPrefix"), prefixDefaults.getString("defaultSuffix")));
    ServerNameUtil.init();

    if (prinLoadScreen) {
      loadScreen();
    }
  }

  public void onDisable() {
    HookManager.removeHook(defaultHookName);
    HookManager.removeHook(storedDataHookName);
    ModuleManager.disableModules();

    proxy.getEventManager().unregisterListener(this, proxyChatAccountManager);
    //proxy.getEventManager().unregisterCommand(proxyChatCommand);
    proxy.getEventManager().unregisterListener(this, channelTypeCorrectorListener);
    proxy.getEventManager().unregisterListener(this, proxyChatEventsListener);
    proxy.getEventManager().unregisterListener(this, mutingListener);

    //proxy.getScheduler().cancel(this);

    // Just to be sure
    proxy.getEventManager().unregisterListeners(this);
    //proxy.getCommandManager().unregisterCommands(this);

    PlaceHolderManager.clear();
    PlaceHolderUtil.clearConfigSections();
    ModuleManager.clearActiveModules();
  }

  @Override
  public File getConfigFolder() {
    if(this.configDir == null) {
      return null;
    }

    File configDir = this.configDir.toFile();

    if (!configDir.exists() && !configDir.mkdirs()) {
        throw new RuntimeException(new IOException("Could not create " + configDir));
    }

    return configDir;
  }

  public File getLangFolder() {
    if (langDir == null) {
      langDir = new File(getConfigFolder(), "lang");

      if (!langDir.exists() && !langDir.mkdirs())
        throw new RuntimeException(new IOException("Could not create " + langDir));
    }

    return langDir;
  }

  @Override
  public void sendPrivateMessage(ProxyChatContext context) throws InvalidContextError {
    MessagesService.sendPrivateMessage(context);
  }

  @Override
  public void sendChannelMessage(ProxyChatContext context, ChannelType channel)
      throws InvalidContextError {
    MessagesService.sendChannelMessage(context, channel);
  }

  private void loadScreen() {
    StartupBannerSize size =
        StartupBannerSize.optionalValueOf(
                Configuration.get().getString("Miscellaneous.startupBannerSize"))
            .orElse(StartupBannerSize.NORMAL);

    if (size == StartupBannerSize.NONE) return;

    if (size != StartupBannerSize.SHORT) {
      LoggerHelper.info(
              NamedTextColor.GOLD
              + "---------------- "
              + NamedTextColor.AQUA
              + "Proxy Chat"
              + NamedTextColor.GOLD
              + " ----------------");
      LoggerHelper.info(getPeopleMessage("Authors", ProxyChatApi.AUTHORS));
    }

    if (size == StartupBannerSize.LONG) {
      LoggerHelper.info(NamedTextColor.YELLOW + "Modules:");

      ModuleManager.getAvailableModulesStream()
          .map(
              module -> {
                if (module.isEnabled()) return "\t" + NamedTextColor.GREEN + "On  - " + module.getName();
                else return "\t" + NamedTextColor.RED + "Off - " + module.getName();
              })
          .forEachOrdered(LoggerHelper::info);
    } else {
      LoggerHelper.info(
          NamedTextColor.YELLOW
              + "Modules: "
              + NamedTextColor.GREEN
              + ProxyChatModuleManager.getActiveModuleString());
    }

    if (size != StartupBannerSize.SHORT) {
      LoggerHelper.info(getPeopleMessage("Contributors", ProxyChatApi.CONTRIBUTORS));
      LoggerHelper.info(getPeopleMessage("Translators", ProxyChatApi.TRANSLATORS));
      LoggerHelper.info(getPeopleMessage("Donators", ProxyChatApi.DONATORS));
    }

    if (size != StartupBannerSize.SHORT) {
      LoggerHelper.info(NamedTextColor.GOLD + "---------------------------------------------");
    }
  }

  private String getPeopleMessage(String name, String... people) {
    return Arrays.stream(people)
        .collect(
            Collectors.joining(
                    ProxyChatModuleManager.MODULE_CONCATENATOR,
                    NamedTextColor.YELLOW + name + ": " + NamedTextColor.GREEN,
                    ""));
  }

  public Logger getLogger() {
    return logger;
  }

  public ProxyServer getProxy() {
    return proxy;
  }

  public ProxyDiscordHandler getProxyDiscordHandler() {
    return proxyDiscordHandler;
  }

  private enum StartupBannerSize {
    NONE,
    SHORT,
    NORMAL,
    LONG;

    public static Optional<StartupBannerSize> optionalValueOf(String value) {
      for (StartupBannerSize element : values()) {
        if (element.name().equalsIgnoreCase(value)) return Optional.of(element);
      }

      return Optional.empty();
    }
  }

  public static ProxyChat getInstance() {
    return ProxyChat.instance;
  }

  private static void setInstance(final ProxyChat instance) {
    ProxyChat.instance = instance;
  }
}
