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

package uk.co.notnull.ProxyChat.module;

import com.velocitypowered.api.command.SimpleCommand;
import lombok.experimental.Delegate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import uk.co.notnull.ProxyChat.api.filter.FilterManager;
import uk.co.notnull.ProxyChat.api.filter.ProxyChatFilter;
import uk.co.notnull.ProxyChat.command.EmojiCommand;
import uk.co.notnull.ProxyChat.filter.EmojiFilter;
import uk.co.notnull.ProxyChat.filter.EmojiPostFilter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class EmojiModule extends Module {
	@Delegate(excludes = ProxyChatFilter.class)

	private final Pattern incompleteEmojiPattern = Pattern.compile("(.*):(\\w+)$");
	private final Pattern emojiPattern = Pattern.compile(":(\\w+):");

	private Pattern characterPattern;

	private final EmojiFilter emojiFilter = new EmojiFilter(this);
	private final EmojiPostFilter emojiPostFilter = new EmojiPostFilter(this);

	private TreeMap<String, Emoji> emojiByName;
	private Map<String, Emoji> emojiByCharacter;
	private Map<String, List<Emoji>> customEmojiByCategory;
	private Component emojiList;

	private EmojiCommand emojiCommand;

	@Override
	public String getName() {
		return "Emoji";
	}

	@Override
	public void onEnable() {
		emojiByName = new TreeMap<>();
		emojiByCharacter = new HashMap<>();
		customEmojiByCategory = new HashMap<>();

		parseConfig();

		emojiCommand = new EmojiCommand(this);

		FilterManager.addPreParseFilter(getName(), emojiFilter);
		FilterManager.addPostParseFilter(getName(), emojiPostFilter);

		emojiCommand.register();


	}

	@Override
	public void onDisable() {
		emojiByName.clear();
		emojiByCharacter.clear();
		customEmojiByCategory.clear();
		emojiCommand.unregister();
		emojiList = null;

		FilterManager.removePreParseFilter(getName());
		FilterManager.removePostParseFilter(getName());
	}

	public void parseConfig() {
		Map<String, Map<String, List<String>>> categories = (Map<String, Map<String, List<String>>>) getModuleSection()
				.getAnyRef("emoji");

		StringBuilder characterRegex = new StringBuilder("[");

		categories.forEach((String category, Map<String, List<String>> emoji) -> {
			ArrayList<Emoji> categoryEmojis = new ArrayList<>();

			emoji.forEach((String character, List<String> names) -> {
				characterRegex.append(character);
				Emoji e = new Emoji(character, names, category);

				names.forEach(name -> this.emojiByName.put(name.toLowerCase(), e));
				emojiByCharacter.put(character, e);
				categoryEmojis.add(e);
			});

			customEmojiByCategory.put(category, categoryEmojis);
		});

		characterRegex.append("]");

		if(characterRegex.length() > 2) {
			characterPattern = Pattern.compile(characterRegex.toString());
		}
	}

	public List<String> getEmojiSuggestions(SimpleCommand.Invocation invocation) {
		String lastWord = invocation.arguments()[invocation.arguments().length - 1].toLowerCase();
		Matcher matcher = incompleteEmojiPattern.matcher(lastWord);

		if(!matcher.find()) {
		  	return Collections.emptyList();
		}

		String prefix = matcher.group(1);

		return searchEmoji(matcher.group(2))
				.stream()
				.map(emoji -> prefix + emoji.getCharacter())
				.collect(Collectors.toList());
	}

	public List<Emoji> searchEmoji(String search) {
		Map<String, Emoji> results = emojiByName.subMap(search, search + Character.MAX_VALUE);

		return results.values().stream().distinct().collect(Collectors.toList());
	}

	public Component getEmojiListComponent() {
		if(emojiList != null) {
			return emojiList;
		}

		TextComponent.Builder list = Component.text().append(
				Component.text().content("Available Emoji")
						.color(NamedTextColor.YELLOW)
						.decoration(TextDecoration.UNDERLINED, TextDecoration.State.TRUE)
						.decoration(TextDecoration.BOLD, TextDecoration.State.TRUE))
				.append(Component.newline());

		customEmojiByCategory.forEach((String category, List<Emoji> emojis) -> {
			if(emojis.isEmpty()) {
				return;
			}

			list.append(Component.text().content(category).color(NamedTextColor.BLUE));
			list.append(Component.newline());
			list.append(Component.space());

			emojis.forEach(emoji -> list.append(emoji.getComponent()).append(Component.space()));
			list.append(Component.newline());
		});

		emojiList = list.build();

		return emojiList;
	}

	public Pattern getEmojiPattern() {
		return emojiPattern;
	}

	public Pattern getCharacterPattern() {
		return characterPattern;
	}

	public Optional<Emoji> getEmojiByName(String name) {
		return Optional.ofNullable(emojiByName.get(name));
	}

	public Optional<Emoji> getEmojiByCharacter(String character) {
		return Optional.ofNullable(emojiByCharacter.get(character));
	}

	public EmojiFilter getEmojiFilter() {
		return emojiFilter;
	}

	public EmojiPostFilter getEmojiPostFilter() {
		return emojiPostFilter;
	}

	@SuppressWarnings("unused")
	public static class Emoji {
		private final List<String> names;
		private final String category;
		private final String character;
		private Component component;

		Emoji(String character, List<String> names, String category) {
			this.character = character;
			this.names = names;
			this.category = category;
		}

		public List<String> getNames() {
			return names;
		}

		public String getPrimaryName() {
			return names.get(0);
		}

		public String getCategory() {
			return category;
		}

		public String getCharacter() {
			return character;
		}

		public Component getComponent() {
			if (component != null) {
				return component;
			}

			this.component = Component.text().content(character)
					.hoverEvent(Component.text()
										.content(character + " " + names.get(0))
										.append(Component.newline())
										.append(Component.text(category,
															   Style.style().color(NamedTextColor.BLUE).build())
										)
										.append(Component.newline())
										.append(Component.text()
														.content(String.join(", ", names))
														.color(NamedTextColor.GRAY)
														.decoration(TextDecoration.ITALIC, TextDecoration.State.TRUE)
														.build())
										.append(Component.newline())
										.append(Component.text("Click to copy",
															   Style.style().color(NamedTextColor.YELLOW).build())
										)
										.append(Component.newline())
										.append(Component.text("Shift + Click to use",
															   Style.style().color(NamedTextColor.YELLOW).build())
										)
										.build()
					)
					.clickEvent(ClickEvent.copyToClipboard(character))
					.insertion(character)
					.build();

			return component;
		}
	}


}
