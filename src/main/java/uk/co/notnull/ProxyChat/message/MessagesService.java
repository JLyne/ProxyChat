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
import net.kyori.adventure.identity.Identified;
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
					.ifPresent((Component message) -> MessagesService.sendMessage(target, senderAccount, message));

			if (ModuleManager.isModuleActive(ProxyChatModuleManager.SPY_MODULE)
					&& !senderAccount.hasPermission(Permission.COMMAND_SOCIALSPY_EXEMPT)) {

				preProcessMessage(context, Format.SOCIAL_SPY, false)
						.ifPresent((Component socialSpyMessage) ->
										   sendToMatchingPlayers(
												   socialSpyMessage,
												   senderAccount,
												   acc -> (!acc.getUniqueId().equals(senderAccount.getUniqueId()))
														   && (!acc.getUniqueId().equals(targetAccount.getUniqueId()))
														   && acc.hasSocialSpyEnabled()));
			}
		}

		if (ProxyChatModuleManager.CHAT_LOGGING_MODULE
				.getModuleSection()
				.getBoolean("privateMessages")) {
			ChatLoggingManager.logMessage("PM to " + targetAccount.getName(), context);
		}
	}

	public static void sendChannelMessage(CommandSource sender, ChannelType channel, String message) throws InvalidContextError {
		ProxyChatContext context = new Context(sender, message);
		boolean allowed = parseMessage(context, true);

		if(allowed) {
			sendChannelMessage(context, channel);
		}
	}

	public static void sendChannelMessage(ProxyChatContext context, ChannelType channel)
			throws InvalidContextError {
		context.require(ProxyChatContext.HAS_SENDER, ProxyChatContext.HAS_MESSAGE, ProxyChatContext.IS_PARSED);

		switch (channel) {
			case GLOBAL:
				sendGlobalMessage(context);
				break;
			case LOCAL:
				sendLocalMessage(context);
				break;
			case STAFF:
				sendStaffMessage(context);
				break;
			default:
				// Ignore
				break;
		}
	}

	public static void sendGlobalMessage(CommandSource sender, String message) throws InvalidContextError {
		ProxyChatContext context = new Context(sender, message);
		boolean allowed = parseMessage(context, true);

		if(allowed) {
			sendGlobalMessage(context);
		}
	}

	public static void sendGlobalMessage(ProxyChatContext context) throws InvalidContextError {
		context.require(ProxyChatContext.HAS_SENDER, ProxyChatContext.HAS_MESSAGE, ProxyChatContext.IS_PARSED);

		ProxyChatAccount sender = context.getSender().orElseThrow();
		Predicate<ProxyChatAccount> global = PredicateUtil.getGlobalPredicate();
		Predicate<ProxyChatAccount> notIgnored = PredicateUtil.getNotIgnoredPredicate(sender);

		preProcessMessage(context, Format.GLOBAL_CHAT)
				.ifPresent((Component message) -> sendToMatchingPlayers(message, sender, global, notIgnored));

		ChatLoggingManager.logMessage(ChannelType.GLOBAL, context);
	}

	public static void sendLocalMessage(CommandSource sender, String message) throws InvalidContextError {
		ProxyChatContext context = new Context(sender, message);
		boolean allowed = parseMessage(context, true);

		if(allowed) {
			sendLocalMessage(context);
		}
	}

	public static void sendLocalMessage(ProxyChatContext context) throws InvalidContextError {
		context.require(ProxyChatContext.HAS_SENDER, ProxyChatContext.HAS_MESSAGE, ProxyChatContext.IS_PARSED);

		ProxyChatAccount account = context.getSender().orElseThrow();
		RegisteredServer localServer = context.getServer().orElse(account.getServer().orElse(null));

		Predicate<ProxyChatAccount> isLocal = PredicateUtil.getLocalPredicate(localServer);
		Predicate<ProxyChatAccount> notIgnored = PredicateUtil.getNotIgnoredPredicate(account);

		preProcessMessage(context, Format.LOCAL_CHAT)
				.ifPresent((Component finalMessage) ->
								   sendToMatchingPlayers(finalMessage, context.getSender().get(), isLocal, notIgnored));

		ChatLoggingManager.logMessage(ChannelType.LOCAL, context);

		if (ModuleManager.isModuleActive(ProxyChatModuleManager.SPY_MODULE)) {
			preProcessMessage(context, Format.LOCAL_SPY, false)
					.ifPresent((Component message) ->
									   sendToMatchingPlayers(message, account, ProxyChatAccount::hasLocalSpyEnabled,
															 isLocal.negate(), notIgnored));
		}
	}

	public static void sendTransparentMessage(ProxyChatContext context) throws InvalidContextError {
		context.require(ProxyChatContext.HAS_SENDER, ProxyChatContext.HAS_MESSAGE, ProxyChatContext.IS_PARSED);

		ProxyChatAccount account = context.getSender().orElseThrow();
		RegisteredServer localServer = context.getServer().orElse(account.getServer().orElse(null));
		Predicate<ProxyChatAccount> isLocal = PredicateUtil.getLocalPredicate(localServer);

		ChatLoggingManager.logMessage(ChannelType.LOCAL, context);

		if (ModuleManager.isModuleActive(ProxyChatModuleManager.SPY_MODULE)
				&& !account.hasPermission(Permission.COMMAND_LOCALSPY_EXEMPT)) {
			preProcessMessage(context, Format.LOCAL_SPY, false)
					.ifPresent((Component message) ->
									   sendToMatchingPlayers(message, account,
															 ProxyChatAccount::hasLocalSpyEnabled, isLocal.negate()));
		}
	}

	public static void sendStaffMessage(CommandSource sender, String message) throws InvalidContextError {
		ProxyChatContext context = new Context(sender, message);
		boolean allowed = parseMessage(context, true);

		if(allowed) {
			sendStaffMessage(context);
		}
	}

	public static void sendStaffMessage(ProxyChatContext context) throws InvalidContextError {
		context.require(ProxyChatContext.HAS_SENDER, ProxyChatContext.HAS_MESSAGE, ProxyChatContext.IS_PARSED);

		preProcessMessage(context, Format.STAFF_CHAT)
				.ifPresent((Component finalMessage) ->
								   sendToMatchingPlayers(finalMessage, context.getSender().orElseThrow(),
														 pp -> pp.hasPermission(Permission.COMMAND_STAFFCHAT_VIEW)));

		ChatLoggingManager.logMessage(ChannelType.STAFF, context);
	}

	public static void sendJoinMessage(Player player) throws InvalidContextError {
		ProxyChatContext context = new Context(player);

		String message = Format.JOIN_MESSAGE.getRaw(context);
		Predicate<ProxyChatAccount> predicate = PredicateUtil.getPermissionPredicate(Permission.MESSAGE_JOIN_VIEW);

		// This condition checks if the player is present and vanished
		if (context.getSender().filter(ProxyChatAccount::isVanished).isPresent()) {
			predicate = predicate.and(PredicateUtil.getPermissionPredicate(Permission.COMMAND_VANISH_VIEW));
		}

		context.setMessage(message);

		if(MessagesService.parseMessage(context, false)) {
			sendToMatchingPlayers(context.getParsedMessage().orElseThrow(), predicate);
		}

		ChatLoggingManager.logMessage("JOIN", context);
	}

	public static void sendLeaveMessage(Player player) throws InvalidContextError {
		ProxyChatContext context = new Context(player);

		String message = Format.LEAVE_MESSAGE.getRaw(context);
		Predicate<ProxyChatAccount> predicate = PredicateUtil.getPermissionPredicate(Permission.MESSAGE_LEAVE_VIEW);

		// This condition checks if the player is present and vanished
		if (context.getSender().filter(ProxyChatAccount::isVanished).isPresent()) {
			predicate = predicate.and(PredicateUtil.getPermissionPredicate(Permission.COMMAND_VANISH_VIEW));
		}

		context.setMessage(message);

		if(MessagesService.parseMessage(context, false)) {
			sendToMatchingPlayers(context.getParsedMessage().orElseThrow(), predicate);
		}

		ChatLoggingManager.logMessage("LEAVE", context);
	}

	public static void sendSwitchMessage(Player player, RegisteredServer server) throws InvalidContextError {
		ProxyChatContext context = new Context(player);
		context.setServer(server);

		String message = Format.SERVER_SWITCH.getRaw(context);
		Predicate<ProxyChatAccount> predicate = PredicateUtil.getPermissionPredicate(Permission.MESSAGE_SWITCH_VIEW);

		// This condition checks if the player is present and vanished
		if (context.getSender().filter(ProxyChatAccount::isVanished).isPresent()) {
			predicate = predicate.and(PredicateUtil.getPermissionPredicate(Permission.COMMAND_VANISH_VIEW));
		}

		context.setMessage(message);

		if(MessagesService.parseMessage(context, false)) {
			sendToMatchingPlayers(context.getParsedMessage().orElseThrow(), predicate);
		}

		ChatLoggingManager.logMessage("SWITCH", context);
	}

	public static Optional<Component> preProcessMessage(ProxyChatContext context, Format format) throws InvalidContextError {
		return preProcessMessage(context, format, true);
	}

	public static Optional<Component> preProcessMessage(
			ProxyChatContext context,
			Format format,
			boolean runFilters) {
		return preProcessMessage(context, format, runFilters, false);
	}

	public static Optional<Component> preProcessMessage(
			ProxyChatContext context,
			Format format,
			boolean runFilters,
			boolean ignoreBlockMessageExceptions)
			throws InvalidContextError {
		context.require(ProxyChatContext.HAS_SENDER, ProxyChatContext.HAS_MESSAGE, ProxyChatContext.IS_PARSED);

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

		return Optional.of(PlaceHolderUtil.getFullFormatMessage(format, context));
	}

	public static boolean parseMessage(ProxyChatContext context, boolean runFilters) {
		context.require(ProxyChatContext.HAS_MESSAGE, ProxyChatContext.HAS_SENDER);

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

	@SafeVarargs
	@SuppressWarnings("varargs")
	public static void sendToMatchingPlayers(Component finalMessage, Identified sender, Predicate<ProxyChatAccount>... playerFilters) {
		Predicate<ProxyChatAccount> playerFiler =
				Arrays.stream(playerFilters).reduce(Predicate::and).orElse(acc -> true);

		AccountManager.getPlayerAccounts().stream()
				.filter(playerFiler)
				.forEach(account ->
								 ProxyChatAccountManager.getCommandSource(account).ifPresent(commandSource ->
																									  MessagesService.sendMessage(
																											  commandSource,
																											  sender,
																											  finalMessage)));
	}

	public static void sendMessage(CommandSource recipient, Component message) {
		if ((message == null)) return;

		recipient.sendMessage(message);
	}

	public static void sendMessage(CommandSource recipient, Identified sender, Component message) {
		if ((message == null)) return;

		recipient.sendMessage(message);
	}

	private MessagesService() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
}
