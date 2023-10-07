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

package uk.co.notnull.ProxyChat.velocity;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.config.ProxyConfig;
import com.velocitypowered.api.proxy.messages.ChannelRegistrar;
import com.velocitypowered.api.proxy.player.ResourcePackInfo;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.scheduler.Scheduler;
import com.velocitypowered.api.util.ProxyVersion;
import net.kyori.adventure.text.Component;
import org.apache.commons.collections4.map.LinkedMap;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class DummyProxyServer implements ProxyServer {
    private final Map<String, RegisteredServer> servers = new LinkedMap<>();

    private final PluginManager pluginManager;

    private final File pluginsFolder =
            new File(System.getProperty("java.io.tmpdir"), "ProxyChatTest/" + UUID.randomUUID());

    private final Logger logger = Logger.getLogger("DummyProxyServer");

    private final CommandManager commandManager = new DummyCommandManager();

    private final Scheduler scheduler = new DummyScheduler();

    private final EventManager eventManager = new DummyEventManager();

    public DummyProxyServer() {
      pluginManager = new DummyPluginManager();
    }

    @Override
    public void shutdown(Component reason) {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public Optional<Player> getPlayer(String username) {
      return Optional.empty();
    }

    @Override
    public Optional<Player> getPlayer(UUID uuid) {
      return Optional.empty();
    }

    @Override
    public Collection<Player> getAllPlayers() {
      return null;
    }

    @Override
    public int getPlayerCount() {
      return 0;
    }

    @Override
    public Optional<RegisteredServer> getServer(String name) {
      return Optional.ofNullable(servers.get(name));
    }

    @Override
    public Collection<RegisteredServer> getAllServers() {
      return servers.values();
    }

    @Override
    public Collection<Player> matchPlayer(String partialName) {
      return null;
    }

    @Override
    public Collection<RegisteredServer> matchServer(String partialName) {
      return null;
    }

    @Override
    public RegisteredServer createRawRegisteredServer(ServerInfo server) {
        return null;
    }

    @Override
    public RegisteredServer registerServer(ServerInfo server) {
      return null;
    }

    @Override
    public void unregisterServer(ServerInfo server) {

    }

    @Override
    public ConsoleCommandSource getConsoleCommandSource() {
      return null;
    }

    @Override
    public ChannelRegistrar getChannelRegistrar() {
      return null;
    }

    @Override
    public InetSocketAddress getBoundAddress() {
      return null;
    }

    @Override
    public ProxyConfig getConfiguration() {
      return null;
    }

    @Override
    public ProxyVersion getVersion() {
      return null;
    }

    @Override
    public ResourcePackInfo.Builder createResourcePackBuilder(String url) {
        return null;
    }

    public void addServer(String name, RegisteredServer server) {
        servers.put(name, server);
    }

    public PluginManager getPluginManager() {
        return this.pluginManager;
    }

    public File getPluginsFolder() {
        return this.pluginsFolder;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }
}
