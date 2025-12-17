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

import com.mojang.brigadier.suggestion.Suggestions;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DummyCommandManager implements CommandManager {

	private CommandMeta.Builder mockBuilder() {
		CommandMeta.Builder mock = Mockito.mock(CommandMeta.Builder.class);
		Mockito.when(mock.aliases()).thenReturn(mock);
		Mockito.when(mock.aliases(ArgumentMatchers.<String>any())).thenReturn(mock);
		Mockito.when(mock.hint(ArgumentMatchers.any())).thenReturn(mock);
		Mockito.when(mock.plugin(Mockito.any(Object.class))).thenReturn(mock);
		Mockito.when(mock.build()).thenReturn(Mockito.mock(CommandMeta.class));

		return mock;
	}

	@Override
	public CommandMeta.Builder metaBuilder(String s) {
		return mockBuilder();
	}

	@Override
	public CommandMeta.Builder metaBuilder(BrigadierCommand brigadierCommand) {
		return mockBuilder();
	}

	@Override
	public void register(BrigadierCommand brigadierCommand) {

	}

	@Override
	public void register(CommandMeta commandMeta, Command command) {

	}

	@Override
	public void unregister(String s) {

	}

	@Override
	public void unregister(CommandMeta commandMeta) {

	}

	@Override
	public @Nullable CommandMeta getCommandMeta(String s) {
		return null;
	}

	@Override
	public CompletableFuture<Boolean> executeAsync(CommandSource commandSource, String s) {
		return null;
	}

	@Override
	public CompletableFuture<Boolean> executeImmediatelyAsync(CommandSource commandSource, String s) {
		return null;
	}

	@Override
	public CompletableFuture<List<String>> offerSuggestions(CommandSource commandSource, String s) {
		return null;
	}

	@Override
	public CompletableFuture<Suggestions> offerBrigadierSuggestions(CommandSource commandSource, String s) {
		return null;
	}

	@Override
	public Collection<String> getAliases() {
		return null;
	}

	@Override
	public boolean hasCommand(String s) {
		return false;
	}

	@Override
	public boolean hasCommand(String s, CommandSource commandSource) {
		return false;
	}
}
