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

package uk.co.notnull.ProxyChat.filter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.filter.FilterManager;
import uk.co.notnull.ProxyChat.api.filter.ProxyChatPostParseFilter;
import uk.co.notnull.ProxyChat.api.permission.Permission;
import uk.co.notnull.ProxyChat.emoji.Emoji;
import uk.co.notnull.ProxyChat.module.EmojiModule;

import java.util.Locale;
import java.util.regex.MatchResult;

public class EmojiPostFilter implements ProxyChatPostParseFilter {
	private final TextReplacementConfig nameReplacement;
	private final TextReplacementConfig characterReplacement;
	private final boolean noPermissions;

	public EmojiPostFilter(EmojiModule module) {
		this(module, false);
	}

	public EmojiPostFilter(EmojiModule module, boolean noPermissions) {
		//Replace custom emoji names with the emoji's component
		nameReplacement = TextReplacementConfig.builder()
				.match(module.getEmojiPattern())
				.replacement((MatchResult result, TextComponent.Builder builder) -> module.getEmoji(
						result.group(1).toLowerCase(Locale.ROOT))
						.map(Emoji::getComponent)
						.orElse(builder.build()))
				.build();

		//Replace default emoji characters with the emoji's component
		characterReplacement = TextReplacementConfig.builder()
				.match(module.getDefaultCharacterPattern())
				.replacement((MatchResult result, TextComponent.Builder builder) ->
									 module.getEmojiByCharacter(result.group(1))
											 .map(Emoji::getComponent)
											 .orElse(builder.build()))
				.build();

		this.noPermissions = noPermissions;
	}

	@Override
	public Component applyFilter(ProxyChatAccount sender, Component message) {
		if(!noPermissions && sender.hasPermission(Permission.USE_EMOJI)) {
			return message;
		}

		return message.replaceText(characterReplacement).replaceText(nameReplacement);
	}

	@Override
	public int getPriority() {
		return FilterManager.EMOJI_FILTER_PRIORITY;
	}
}
