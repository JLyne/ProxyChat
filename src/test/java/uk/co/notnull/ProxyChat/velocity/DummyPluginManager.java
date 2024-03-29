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

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginManager;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public class DummyPluginManager implements PluginManager {
	@Override
	public Optional<PluginContainer> fromInstance(Object instance) {
		return Optional.empty();
	}

	@Override
	public Optional<PluginContainer> getPlugin(String id) {
		return Optional.empty();
	}

	@Override
	public Collection<PluginContainer> getPlugins() {
		return null;
	}

	@Override
	public boolean isLoaded(String id) {
		return false;
	}

	@Override
	public void addToClasspath(Object plugin, Path path) {

	}
}
