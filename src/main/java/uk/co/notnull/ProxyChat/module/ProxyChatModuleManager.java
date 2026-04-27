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

import uk.co.notnull.ProxyChat.api.module.ProxyChatModule;
import uk.co.notnull.ProxyChat.api.module.ModuleManager;
import uk.co.notnull.ProxyChat.module.perms.LuckPerms5Module;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProxyChatModuleManager extends ModuleManager {
  // PermissionHookModules
  public static final LuckPerms5Module LUCK_PERMS_5_MODULE = new LuckPerms5Module();
  // Normal Modules
  public static final AlertModule ALERT_MODULE = new AlertModule();
  public static final AntiAdvertisingModule ANTI_ADVERTISING_MODULE = new AntiAdvertisingModule();
  public static final AntiCapslockModule ANTI_CAPSLOCK_MODULE = new AntiCapslockModule();
  public static final AntiDuplicationModule ANTI_DUPLICATION_MODULE = new AntiDuplicationModule();
  public static final AntiSpamModule ANTI_SPAM_MODULE = new AntiSpamModule();
  public static final AntiSwearModule ANTI_SWEAR_MODULE = new AntiSwearModule();
  public static final AutoBroadcastModule AUTO_BROADCAST_MODULE = new AutoBroadcastModule();
  public static final ChatLockModule CHAT_LOCK_MODULE = new ChatLockModule();
  public static final ChatLoggingModule CHAT_LOGGING_MODULE = new ChatLoggingModule();
  public static final ClearChatModule CLEAR_CHAT_MODULE = new ClearChatModule();
  public static final GlobalChatModule GLOBAL_CHAT_MODULE = new GlobalChatModule();
  public static final IgnoringModule IGNORING_MODULE = new IgnoringModule();
  public static final JoinMessageModule JOIN_MESSAGE_MODULE = new JoinMessageModule();
  public static final LeaveMessageModule LEAVE_MESSAGE_MODULE = new LeaveMessageModule();
  public static final LocalChatModule LOCAL_CHAT_MODULE = new LocalChatModule();
  public static final BridgeModule BRIDGE_MODULE = new BridgeModule();
  public static final LocalToModule LOCAL_TO_MODULE = new LocalToModule();
  public static final MessengerModule MESSENGER_MODULE = new MessengerModule();
  public static final MulticastChatModule MULTICAST_CHAT_MODULE = new MulticastChatModule();
  public static final MOTDModule MOTD_MODULE = new MOTDModule();
  public static final MutingModule MUTING_MODULE = new MutingModule();
  public static final ServerSwitchModule SERVER_SWITCH_MODULE = new ServerSwitchModule();
  public static final SpyModule SPY_MODULE = new SpyModule();
  public static final StaffChatModule STAFF_CHAT_MODULE = new StaffChatModule();
  public static final VanishModule VANISHER_MODULE = new VanishModule();
  public static final WelcomeMessageModule WELCOME_MESSAGE_MODULE = new WelcomeMessageModule();
  public static final EmojiModule EMOJI_MODULE = new EmojiModule();
  public static final PlatformModule PLATFORM_MODULE = new PlatformModule();
  public static final String MODULE_CONCATENATOR = NamedTextColor.WHITE + ", " + NamedTextColor.GREEN;

  private static boolean modulesAdded = false;
  private static List<ProxyChatModule> localModules = null;

  public static List<ProxyChatModule> getLocalModules() {
    if (localModules == null) {
      localModules =
          Arrays.stream(ProxyChatModuleManager.class.getDeclaredFields())
              .filter(field -> ProxyChatModule.class.isAssignableFrom(field.getType()))
              .map(
                  field -> {
                    try {
                      return (ProxyChatModule) field.get(null);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                      e.printStackTrace();

                      return null;
                    }
                  })
              .collect(Collectors.toList());
    }

    return localModules;
  }

  public static void registerPluginModules() {
    if (!modulesAdded) {
      getAvailableModules().addAll(0, getLocalModules());

      modulesAdded = true;
    }
  }

  public static String getActiveModuleString() {
    return getActiveModulesStream()
        .map(ProxyChatModule::getName)
        .collect(Collectors.joining(MODULE_CONCATENATOR));
  }
}
