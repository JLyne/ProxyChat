package dev.aura.bungeechat.message;

import com.velocitypowered.api.command.CommandSource;
import dev.aura.bungeechat.api.account.BungeeChatAccount;
import dev.aura.bungeechat.api.placeholder.BungeeChatContext;
import dev.aura.lib.messagestranslator.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor
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

  // Prefix/Suffix Messages
  PREFIX_REMOVED("prefixRemoved"),
  PREFIX_SET("prefixSet"),
  SUFFIX_REMOVED("suffixRemoved"),
  SUFFIX_SET("suffixSet"),

  // Update available Message
  UPDATE_AVAILABLE("updateAvailable");

  @Getter private final String stringPath;

  public Component get() {
    return PlaceHolderUtil.getFullMessage(this);
  }

  public Component get(BungeeChatAccount sender) {
    return get(new BungeeChatContext(sender));
  }

  public Component get(BungeeChatAccount sender, String command) {
    return get(new BungeeChatContext(sender, command));
  }

  public Component get(BungeeChatContext context) {
    return PlaceHolderUtil.getFullMessage(this, context);
  }

  public Component get(CommandSource sender) {
    return get(new Context(sender));
  }

  public Component get(CommandSource sender, String command) {
    return get(new Context(sender, command));
  }
}
