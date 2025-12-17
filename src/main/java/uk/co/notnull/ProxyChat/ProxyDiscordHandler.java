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

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import uk.co.notnull.ProxyChat.account.ProxyChatAccountManager;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.enums.ChannelType;
import uk.co.notnull.ProxyChat.api.placeholder.ProxyChatContext;
import uk.co.notnull.ProxyChat.chatlog.ChatLogger;
import uk.co.notnull.ProxyChat.chatlog.ChatLoggingManager;
import uk.co.notnull.ProxyChat.emoji.Emoji;
import uk.co.notnull.ProxyChat.module.EmojiModule;
import uk.co.notnull.ProxyChat.module.ProxyChatModuleManager;
import uk.co.notnull.proxydiscord.api.ProxyDiscord;
import uk.co.notnull.proxydiscord.api.events.DiscordChatEvent;
import uk.co.notnull.proxydiscord.api.events.DiscordLogEvent;
import uk.co.notnull.proxydiscord.api.logging.LogEntry;
import uk.co.notnull.proxydiscord.api.logging.LogType;
import uk.co.notnull.proxydiscord.api.logging.LogVisibility;
import java.util.Map;
import java.util.Optional;

public class ProxyDiscordHandler implements ChatLogger {
	private final ProxyChat plugin;
	private final ProxyDiscord proxyDiscord;
	private final EmojiModule emojiModule = ProxyChatModuleManager.EMOJI_MODULE;

	public ProxyDiscordHandler(ProxyChat plugin) {
		this.plugin = plugin;
		this.proxyDiscord = (ProxyDiscord) plugin.getProxy().getPluginManager()
				.getPlugin("proxydiscord").orElseThrow().getInstance().orElseThrow();

		this.proxyDiscord.setEmoteProvider((s, builder) -> {
			if(emojiModule.isEnabled()) {
				return emojiModule.getEmoji(s.toLowerCase())
						.map(Emoji::getComponent).orElse(builder.build());
			} else {
				return builder.build();
			}
		});

		ChatLoggingManager.addLogger(this);

		plugin.getProxy().getEventManager().register(plugin, this);
	}

	@Subscribe
	public void onDiscordLog(DiscordLogEvent event) {
		LogType type = event.getLogEntry().getType();

		if(type == LogType.JOIN || type == LogType.LEAVE) {
			Optional<ProxyChatAccount> account = ProxyChatAccountManager.getAccount(event.getLogEntry().getPlayer());

			if(account.isPresent() && account.get().isVanished()) {
				event.setResult(DiscordLogEvent.DiscordLogResult.privateOnly());
			}
		}

		if(type == LogType.COMMAND) {
			String command = event.getLogEntry().getReplacements().get("command");

			if(command.startsWith("l ") || command.startsWith("sc ")
					|| command.startsWith("global ") || command.startsWith("local ")
					|| command.startsWith("localto ")) {
				event.setResult(DiscordLogEvent.DiscordLogResult.denied());
			}
		}
	}

	@Subscribe
	public void onDiscordChat(DiscordChatEvent event) {
		Optional<ProxyChatAccount> account = ProxyChatAccountManager.getAccount(event.getSender().getUniqueId());

		account.ifPresent(acc -> {
			if(acc.isMuted()) {
				event.setResult(PlayerChatEvent.ChatResult.denied());
			}
		});
	}

	public void log(ProxyChatContext context) {
		if(!context.hasSender() || !context.hasChannel()) {
			return;
		}

		Optional<Player> player = context.getSender().flatMap(
				sender -> plugin.getProxy().getPlayer(sender.getUniqueId()));

		if(player.isEmpty() || context.getMessage().isEmpty()) {
			return;
		}

		LogEntry.Builder entry = LogEntry.builder().type(LogType.CHAT).player(player.get())
				.server(context.getServer().orElse(null));

		String unfiltered = context.getMessage().get();
		String filtered = context.getFilteredMessage().orElse(unfiltered);
		ChannelType channel = context.getChannel().orElseThrow();

		if(channel == ChannelType.PRIVATE) {
			return; //Don't log private messages for now
		}

		if(channel == ChannelType.STAFF) {
			entry.visibility(LogVisibility.PRIVATE_ONLY)
					.replacements(Map.of("message", "[STAFF] " + filtered));
		} else if(channel == ChannelType.ALERT) {
			entry.replacements(Map.of("message", "[ALERT] " + filtered));
		} else {
			if(channel == ChannelType.GLOBAL) {
				//TODO: All servers
			}

			if(emojiModule.isEnabled()) {
				unfiltered = emojiModule.getEmojiFilter().applyFilter(context.getSender().get(), unfiltered);
			}

			LogEntry privateEntry = LogEntry.builder(entry).visibility(LogVisibility.PRIVATE_ONLY)
					.replacements(Map.of("message", unfiltered)).build();

			entry.visibility(LogVisibility.PUBLIC_ONLY).replacements(Map.of("message", filtered));
			proxyDiscord.getLoggingManager().logEvent(privateEntry);
		}

		proxyDiscord.getLoggingManager().logEvent(entry.build());
	}
}
