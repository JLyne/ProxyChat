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

import com.google.common.base.Preconditions;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.LoggerFactory;
import uk.co.notnull.ProxyChat.config.Configuration;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import uk.co.notnull.ProxyChat.velocity.DummyPluginDescription;
import uk.co.notnull.ProxyChat.velocity.DummyProxyServer;

public final class TestHelper {
  private static ProxyChat proxyChat;
  private static boolean hasInitRun = false;

  public static void initProxyChat() {
    try {
      if (!hasInitRun) {
        ProxyServer proxyServer = new DummyProxyServer();
        PluginDescription desc = new DummyPluginDescription();
        proxyChat = new ProxyChat(proxyServer, LoggerFactory.getLogger("test"), desc);
        proxyChat.onLoad();
        hasInitRun = true;
      }
      Configuration.load();
    } catch (final java.lang.Throwable $ex) {
      throw lombok.Lombok.sneakyThrow($ex);
    }
  }

  public static void deinitProxyChat() throws IOException {
    File directory = proxyChat.getConfigFolder();

    if(directory == null) {
      return;
    }

    FileUtils.deleteDirectory(directory);
    Preconditions.checkState(
            proxyChat.getConfigFolder().mkdirs(), "Could not create config folder");
  }

  private TestHelper() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
