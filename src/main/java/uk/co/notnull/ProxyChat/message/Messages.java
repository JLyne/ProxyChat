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
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.placeholder.ProxyChatContext;
import dev.aura.lib.messagestranslator.Message;
import net.kyori.adventure.text.Component;

public enum Messages implements Message {
  // Channel Type Messages
  ENABLE_GLOBAL("enableGlobal"),
  ENABLE_STAFFCHAT("enableStaffchat"),
  ENABLE_LOCAL("enableLocal"),
  GLOBAL_IS_DEFAULT("globalIsDefault"),
  LOCAL_IS_DEFAULT("localIsDefault"),
  BACK_TO_DEFAULT("backToDefault"),
  NOT_IN_GLOBAL_SERVER("notInGlobalServer"),
  NOT_IN_LOCAL_SERVER("notInLocalServer"),

  // Messenger Messages
  MESSAGE_YOURSELF("messageYourself"),
  ENABLE_MESSAGER("enableMessager"),
  ENABLE_MESSAGER_OTHERS("enableMessagerOthers"),
  DISABLE_MESSAGER("disableMessager"),
  DISABLE_MESSAGER_OTHERS("disableMessagerOthers"),
  NO_REPLY("noReply"),
  REPLY_OFFLINE("replyOffline"),
  HAS_MESSAGER_DISABLED("hasMessagerDisabled"),

  // Clear Chat
  CLEARED_LOCAL("clearedLocal"),
  CLEARED_GLOBAL("clearedGlobal"),

  // Vanish Messages
  ENABLE_VANISH("enableVanish"),
  DISABLE_VANISH("disableVanish"),

  // Mute Messages
  MUTED("muted"),
  UNMUTE_NOT_MUTED("unmuteNotMuted"),
  MUTE_IS_MUTED("muteIsMuted"),
  UNMUTE("unmute"),
  MUTE("mute"),
  TEMPMUTE("tempmute"),

  // Spy Messages
  ENABLE_SOCIAL_SPY("enableSocialSpy"),
  DISABLE_SOCIAL_SPY("disableSocialSpy"),
  ENABLE_LOCAL_SPY("enableLocalSpy"),
  DISABLE_LOCAL_SPY("disableLocalSpy"),

  // Error Messages
  NOT_A_PLAYER("notPlayer"),
  PLAYER_NOT_FOUND("playerNotFound"),
  INCORRECT_USAGE("incorrectUsage"),
  NO_PERMISSION("noPermission"),
  UNKNOWN_SERVER("unknownServer"),

  // Ignore Messages
  HAS_INGORED("hasIgnored"),
  ADD_IGNORE("addIgnore"),
  REMOVE_IGNORE("removeIgnore"),
  ALREADY_IGNORED("alreadyIgnored"),
  IGNORE_YOURSELF("ignoreYourself"),
  UNIGNORE_YOURSELF("unignoreYourself"),
  NOT_IGNORED("notIgnored"),
  IGNORE_LIST("ignoreList"),
  IGNORE_NOBODY("ignoreNobody"),
  MESSAGE_BLANK("messageBlank"),

  // Filter Messages
  ANTI_ADVERTISE("antiAdvertise"),
  ANTI_CAPSLOCK("antiCapslock"),
  ANTI_DUPLICATION("antiDuplication"),
  ANTI_SPAM("antiSpam"),

  // ChatLock Messages
  ENABLE_CHATLOCK("enableChatlock"),
  DISABLE_CHATLOCK("disableChatlock"),
  CHAT_IS_DISABLED("chatIsLocked"),

  // Misc Messages
  PLUGIN_PREFIX("pluginPrefix"),
  PLUGIN_RELOAD("pluginReloaded"),
  PLUGIN_MODULES("pluginActiveModules"),
  PLUGIN_CREDITS("pluginCredits");

  private final String stringPath;

  public Component get() {
    return PlaceHolderUtil.getFullMessage(this);
  }

  public String getRaw() {
    return PlaceHolderUtil.getFullMessageRaw(this);
  }

  public Component get(ProxyChatAccount sender) {
    return get(new ProxyChatContext(sender));
  }

  public Component get(ProxyChatAccount sender, String command) {
    ProxyChatContext context = new ProxyChatContext(sender, command);
    MessagesService.parseMessage(context, false);
    return get(context);
  }

  public Component get(ProxyChatContext context) {
    return PlaceHolderUtil.getFullMessage(this, context);
  }

  public Component get(CommandSource sender) {
    return get(new Context(sender));
  }

  public Component get(CommandSource sender, String command) {
    return get(new Context(sender, command));
  }

  private Messages(final String stringPath) {
    this.stringPath = stringPath;
  }

  public String getStringPath() {
    return this.stringPath;
  }
}
