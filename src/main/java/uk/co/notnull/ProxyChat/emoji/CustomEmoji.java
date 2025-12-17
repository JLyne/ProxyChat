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

package uk.co.notnull.ProxyChat.emoji;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;

@SuppressWarnings("unused")
public class CustomEmoji extends Emoji {
	private final String category;

	public CustomEmoji(String character, List<String> names, String category) {
		super(character, names, true);
		this.category = category;
	}

	public String getCategory() {
		return category;
	}

	public Component getComponent() {
		if (component != null) {
			return component;
		}

		this.component = Component.text().content(character)
				.hoverEvent(Component.text()
									.content(character + " " + names.getFirst())
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

	@Override
	public String toString() {
		return "CustomEmoji{" +
				"category='" + category + '\'' +
				", names=" + names +
				", character='" + character + '\'' +
				'}';
	}
}
