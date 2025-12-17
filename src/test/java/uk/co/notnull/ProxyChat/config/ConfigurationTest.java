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

package uk.co.notnull.ProxyChat.config;

import static org.junit.Assert.assertEquals;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import uk.co.notnull.ProxyChat.ProxyChat;
import uk.co.notnull.ProxyChat.TestHelper;
import uk.co.notnull.ProxyChat.api.ProxyChatApi;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigurationTest {
  @BeforeClass
  public static void initProxyChat() {
    TestHelper.initProxyChat();
  }

  @AfterClass
  public static void deinitProxyChat() throws IOException {
    TestHelper.deinitProxyChat();
  }

  @Test
  public void versionMatchTest() {
    Config defaultConfig =
        ConfigFactory.parseReader(
            new InputStreamReader(
                    ProxyChat.getInstance().getClass()
                            .getClassLoader().getResourceAsStream(Configuration.CONFIG_FILE_NAME),
                    StandardCharsets.UTF_8),
            Configuration.PARSE_OPTIONS);

    assertEquals(ProxyChatApi.CONFIG_VERSION, defaultConfig.getDouble("Version"), 0.0);
  }
}
