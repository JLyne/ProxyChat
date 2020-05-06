package dev.aura.bungeechat.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import dev.aura.bungeechat.account.BungeecordAccountManager;
import dev.aura.bungeechat.api.account.BungeeChatAccount;
import dev.aura.bungeechat.api.enums.AccountType;
import dev.aura.bungeechat.api.enums.ChannelType;
import dev.aura.bungeechat.message.Messages;
import dev.aura.bungeechat.message.MessagesService;
import dev.aura.bungeechat.module.GlobalChatModule;
import dev.aura.bungeechat.permission.Permission;
import dev.aura.bungeechat.permission.PermissionManager;
import java.util.Arrays;
import java.util.stream.Collectors;

public class GlobalChatCommand extends BaseCommand {
  public GlobalChatCommand(GlobalChatModule globalChatModule) {
    super("global", globalChatModule.getModuleSection().getStringList("aliases"));
  }

  @Override
  public void execute(CommandSource sender, String[] args) {
    if (PermissionManager.hasPermission(sender, Permission.COMMAND_GLOBAL)) {
      BungeeChatAccount account = BungeecordAccountManager.getAccount(sender).get();

      if (!MessagesService.getGlobalPredicate().test(account)
          && (account.getAccountType() == AccountType.PLAYER)) {
        MessagesService.sendMessage(sender, Messages.NOT_IN_GLOBAL_SERVER.get());

        return;
      }

      if (args.length < 1) {
        if (!(sender instanceof Player)) {
          MessagesService.sendMessage(sender, Messages.NOT_A_PLAYER.get());
          return;
        }
        if (PermissionManager.hasPermission(sender, Permission.COMMAND_GLOBAL_TOGGLE)) {
          BungeeChatAccount player = BungeecordAccountManager.getAccount(sender).get();

          if (player.getChannelType() == ChannelType.GLOBAL) {
            ChannelType defaultChannelType = player.getDefaultChannelType();
            player.setChannelType(defaultChannelType);

            if (defaultChannelType == ChannelType.LOCAL) {
              MessagesService.sendMessage(sender, Messages.ENABLE_LOCAL.get());
            } else {
              MessagesService.sendMessage(sender, Messages.GLOBAL_IS_DEFAULT.get());
            }
          } else {
            player.setChannelType(ChannelType.GLOBAL);
            MessagesService.sendMessage(sender, Messages.ENABLE_GLOBAL.get());
          }
        } else {
          MessagesService.sendMessage(
              sender, Messages.INCORRECT_USAGE.get(sender, "/global <message>"));
        }
      } else {
        String finalMessage = Arrays.stream(args).collect(Collectors.joining(" "));

        MessagesService.sendGlobalMessage(sender, finalMessage);
      }
    }
  }
}
