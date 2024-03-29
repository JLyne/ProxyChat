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

package uk.co.notnull.ProxyChat.message;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import uk.co.notnull.ProxyChat.account.ProxyChatAccountManager;
import uk.co.notnull.ProxyChat.api.account.AccountManager;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.enums.ChannelType;
import uk.co.notnull.ProxyChat.api.filter.BlockMessageException;
import uk.co.notnull.ProxyChat.api.filter.FilterManager;
import uk.co.notnull.ProxyChat.api.module.ModuleManager;
import uk.co.notnull.ProxyChat.api.placeholder.ProxyChatContext;
import uk.co.notnull.ProxyChat.api.placeholder.InvalidContextError;
import uk.co.notnull.ProxyChat.chatlog.ChatLoggingManager;
import uk.co.notnull.ProxyChat.module.ProxyChatModuleManager;
import uk.co.notnull.ProxyChat.api.permission.Permission;
import uk.co.notnull.ProxyChat.permission.PermissionManager;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

import net.kyori.adventure.text.Component;
import uk.co.notnull.ProxyChat.util.ComponentUtil;
import uk.co.notnull.ProxyChat.util.PredicateUtil;

public final class MessagesService {
	public static void sendPrivateMessage(CommandSource sender, CommandSource target, String message) throws InvalidContextError {
		ProxyChatContext context = new Context(sender, target, message);
		context.setChannel(ChannelType.PRIVATE);
		boolean allowed = parseMessage(context, true);

		if(allowed) {
			sendPrivateMessage(context);
		}
	}

	public static void sendPrivateMessage(ProxyChatContext context) throws InvalidContextError {
		context.require(ProxyChatContext.HAS_SENDER, ProxyChatContext.HAS_TARGET,
						ProxyChatContext.HAS_MESSAGE, ProxyChatContext.IS_PARSED);

		ProxyChatAccount senderAccount = context.getSender().orElseThrow();
		ProxyChatAccount targetAccount = context.getTarget().orElseThrow();
		CommandSource sender = ProxyChatAccountManager.getCommandSource(senderAccount).orElseThrow();
		CommandSource target = ProxyChatAccountManager.getCommandSource(targetAccount).orElseThrow();
		boolean filterPrivateMessages =
				ProxyChatModuleManager.MESSENGER_MODULE
						.getModuleSection()
						.getBoolean("filterPrivateMessages");

		if (targetAccount.hasIgnored(senderAccount)
				&& !PermissionManager.hasPermission(sender, Permission.BYPASS_IGNORE)) {
			sendMessage(sender, Messages.HAS_INGORED.get(context));

			return;
		}

		Optional<Component> messageSender = preProcessMessage(context, Format.MESSAGE_SENDER, filterPrivateMessages);

		if (messageSender.isPresent()) {
			MessagesService.sendMessage(sender, messageSender.get());

			preProcessMessage(context, Format.MESSAGE_TARGET, filterPrivateMessages, true)
					.ifPresent((Component message) -> MessagesService.sendMessage(target, message));

			if (ModuleManager.isModuleActive(ProxyChatModuleManager.SPY_MODULE)
					&& !senderAccount.hasPermission(Permission.COMMAND_SOCIALSPY_EXEMPT)) {

				preProcessMessage(context, Format.SOCIAL_SPY, false)
						.ifPresent((Component socialSpyMessage) ->
										   sendToMatchingPlayers(
												   socialSpyMessage,
												   acc -> (!acc.getUniqueId().equals(senderAccount.getUniqueId()))
														   && (!acc.getUniqueId().equals(targetAccount.getUniqueId()))
														   && acc.hasSocialSpyEnabled()));
			}
		}

		if (ProxyChatModuleManager.CHAT_LOGGING_MODULE
				.getModuleSection()
				.getBoolean("privateMessages")) {
			ChatLoggingManager.logMessage(context);
		}
	}

	public static void sendChannelMessage(CommandSource sender, ChannelType channel, String message) throws InvalidContextError {
		ProxyChatContext context = new Context(sender, message);
		context.setChannel(channel);
		boolean allowed = parseMessage(context, true);

		if(allowed) {
			sendChannelMessage(context);
		}
	}

	public static void sendChannelMessage(ProxyChatContext context) throws InvalidContextError {
		// Ensure a channel is set
		if(!context.hasChannel()) {
			if (context.hasSender()) {
				context.setChannel(context.getSender().orElseThrow().getChannelType());
			} else {
				context.setChannel(ChannelType.LOCAL);
			}
		}

		context.require(ProxyChatContext.HAS_CHANNEL);
		context.getChannel().orElseThrow().checkRequirements(context);

		//TODO: Better way?
		if(context.getChannel().get().equals(ChannelType.PRIVATE)) {
			sendPrivateMessage(context);
			return;
		}

		ProxyChatAccount sender = context.getSender().orElseThrow();
		Predicate<ProxyChatAccount> recipients = PredicateUtil.getGlobalPredicate();
		ChannelType channel = context.getChannel().orElseThrow();

		switch (channel) {
			case LOCAL -> {
				RegisteredServer server = context.getServer().orElseThrow();
				recipients = PredicateUtil.getServerPredicate(server);
				Predicate<ProxyChatAccount> spyRecipients = PredicateUtil.getInclusiveMulticastPredicate(server).negate();

				//TODO: Move to module?
				if (ModuleManager.isModuleActive(ProxyChatModuleManager.SPY_MODULE)) {
					preProcessMessage(context, Format.LOCAL_SPY, false)
							.ifPresent((Component message) -> sendToMatchingPlayers(
									message, ProxyChatAccount::hasLocalSpyEnabled, spyRecipients));
				}
			}
			case MULTICAST -> recipients = PredicateUtil.getMulticastPredicate(context.getServer().orElseThrow());
			case STAFF -> recipients = pp -> pp.hasPermission(Permission.COMMAND_STAFFCHAT_VIEW);
			case JOIN -> recipients = PredicateUtil.getPermissionPredicate(Permission.MESSAGE_JOIN_VIEW);
			case LEAVE -> recipients = PredicateUtil.getPermissionPredicate(Permission.MESSAGE_LEAVE_VIEW);
			case SWITCH -> recipients = PredicateUtil.getPermissionPredicate(Permission.MESSAGE_SWITCH_VIEW);
		}

		if(channel.isIgnorable()) {
			recipients = recipients.and(PredicateUtil.getNotIgnoredPredicate(sender));
		}

		// This condition checks if the player is present and vanished
		if(channel.isRespectVanish() && sender.isVanished()) {
			recipients = recipients.and(PredicateUtil.getPermissionPredicate(Permission.COMMAND_VANISH_VIEW));
		}

		Predicate<ProxyChatAccount> finalRecipients = recipients;
		preProcessMessage(context).ifPresent((Component message) -> sendToMatchingPlayers(message, finalRecipients));

		if(channel.isLoggable()) {
			ChatLoggingManager.logMessage(context);
		}

		if(channel.equals(ChannelType.LOCAL)) {
			context.setChannel(ChannelType.MULTICAST);
			sendChannelMessage(context);
		}
	}

	public static void sendGlobalMessage(CommandSource sender, String message) throws InvalidContextError {
		sendChannelMessage(sender, ChannelType.GLOBAL, message);
	}

	public static void sendLocalMessage(CommandSource sender, String message) throws InvalidContextError {
		sendChannelMessage(sender, ChannelType.LOCAL, message);
	}

	public static void sendTransparentMessage(ProxyChatContext context) throws InvalidContextError {
		context.require(ProxyChatContext.HAS_SENDER, ProxyChatContext.HAS_MESSAGE, ProxyChatContext.IS_PARSED);

		ProxyChatAccount account = context.getSender().orElseThrow();
		RegisteredServer localServer = context.getServer().orElse(account.getServer().orElse(null));
		Predicate<ProxyChatAccount> isLocal = PredicateUtil.getServerPredicate(localServer);

		ChatLoggingManager.logMessage(context);

		//TODO: Move to module?
		if (ModuleManager.isModuleActive(ProxyChatModuleManager.SPY_MODULE)
				&& !account.hasPermission(Permission.COMMAND_LOCALSPY_EXEMPT)) {
			preProcessMessage(context, Format.LOCAL_SPY, false)
					.ifPresent((Component message) ->
									   sendToMatchingPlayers(message, ProxyChatAccount::hasLocalSpyEnabled, isLocal.negate()));
		}
	}

	public static void sendStaffMessage(CommandSource sender, String message) throws InvalidContextError {
		sendChannelMessage(sender, ChannelType.STAFF, message);
	}

	public static void sendJoinMessage(Player player) throws InvalidContextError {
		sendChannelMessage(player, ChannelType.JOIN, null);
	}

	public static void sendLeaveMessage(Player player) throws InvalidContextError {
		sendChannelMessage(player, ChannelType.LEAVE, null);
	}

	public static void sendSwitchMessage(Player player, RegisteredServer server) throws InvalidContextError {
		sendChannelMessage(player, ChannelType.SWITCH, null); //FIXME: Server?
	}

	public static Optional<Component> preProcessMessage(ProxyChatContext context) throws InvalidContextError {
		context.require(ProxyChatContext.HAS_CHANNEL);
		return preProcessMessage(context, Format.getFormatForChannel(context.getChannel().orElseThrow()), true);
	}

	public static Optional<Component> preProcessMessage(ProxyChatContext context, Format format, boolean runFilters) {
		return preProcessMessage(context, format, runFilters, false);
	}

	public static Optional<Component> preProcessMessage(ProxyChatContext context, Format format,
			boolean runFilters, boolean ignoreBlockMessageExceptions) throws InvalidContextError {
		if(context.hasMessage()) {
			context.require(ProxyChatContext.HAS_SENDER, ProxyChatContext.IS_PARSED);

			ProxyChatAccount account = context.getSender().orElseThrow();
			CommandSource player = ProxyChatAccountManager.getCommandSource(account).orElseThrow();

			Component message = ComponentUtil.filterFormatting(context.getParsedMessage().orElseThrow(), account);

			if (runFilters) {
				try {
					message = FilterManager.applyFilters(account, message);
				} catch (BlockMessageException e) {
					if (!ignoreBlockMessageExceptions) {
						MessagesService.sendMessage(player, e.getComponent());

						return Optional.empty();
					}
				}
			}

			context.setParsedMessage(message);
		}

		return Optional.of(PlaceHolderUtil.getFullFormatMessage(format, context));
	}

	public static boolean parseMessage(ProxyChatContext context, boolean runFilters) {
		if(!context.hasMessage()) {
			return true;
		}

		context.require(ProxyChatContext.HAS_SENDER);

		ProxyChatAccount playerAccount = context.getSender().orElseThrow();
		CommandSource player = ProxyChatAccountManager.getCommandSource(playerAccount).orElseThrow();
		String message = context.getMessage().orElseThrow();

		if(runFilters) {
			try {
				message = FilterManager.applyFilters(playerAccount, message);
				context.setFilteredMessage(message);
			} catch (BlockMessageException e) {
				MessagesService.sendMessage(player, e.getComponent());

				return false;
			}
		}

		context.setParsedMessage(ComponentUtil.extractUrls(
				ComponentUtil.filterFormatting(
						ComponentUtil.untrustedMiniMessage.deserialize(message), playerAccount)));

		return true;
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	public static void sendToMatchingPlayers(Component finalMessage, Predicate<ProxyChatAccount>... playerFilters) {
		Predicate<ProxyChatAccount> playerFiler =
				Arrays.stream(playerFilters).reduce(Predicate::and).orElse(acc -> true);

		AccountManager.getPlayerAccounts().stream()
				.filter(playerFiler)
				.forEach(account ->
								 ProxyChatAccountManager.getCommandSource(account).ifPresent(commandSource ->
																									  MessagesService.sendMessage(
																											  commandSource,
																											  finalMessage)));
	}

	public static void sendMessage(CommandSource recipient, Component message) {
		if ((message == null)) return;

		recipient.sendMessage(message);
	}

	private MessagesService() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
}
