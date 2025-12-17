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

package uk.co.notnull.ProxyChat.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.vorburger.exec.ManagedProcessException;
import com.google.common.collect.ImmutableMap;
import uk.co.notnull.ProxyChat.TestHelper;
import uk.co.notnull.ProxyChat.api.account.AccountInfo;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccountStorage;
import uk.co.notnull.ProxyChat.api.enums.ChannelType;
import uk.co.notnull.ProxyChat.module.Module;
import uk.co.notnull.ProxyChat.testhelpers.TestDatabase;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.*;

public class AccountSQLStorageTest {
  private static final String database = "test";
  private static final String password = "test";
  private static final String username = "test";
  private static final String tablePrefix = "proxychat_";
  private static final ImmutableMap<String, String> defaultOptions =
      ImmutableMap.<String, String>builder()
          .put("useUnicode", "true")
          .put("characterEncoding", "utf8")
          .build();

  private Connection connection;

  @BeforeClass
  public static void setUpBeforeClass() throws ManagedProcessException {
    Module.setTest_mode(true);

    TestHelper.initProxyChat();
    TestDatabase.startDatabase();
  }

  @AfterClass
  public static void tearDownAfterClass() throws ManagedProcessException {
    TestDatabase.stopDatabase();
  }

  @Before
  public void setUp() throws SQLException {
    connection = TestDatabase.getDatabaseInstance();
  }

  @After
  public void tearDown() throws Exception {
    TestDatabase.closeDatabaseInstance(connection);
  }

  @Test
  public void optionsMapToStringTest() {
    final Map<String, String> modifiedOptions = new LinkedHashMap<>(defaultOptions);
    modifiedOptions.put("connectTimeout", "10");

    assertEquals(
        "connectTimeout=0&socketTimeout=0&autoReconnect=true&useUnicode=true&characterEncoding=utf8",
        AccountSQLStorage.optionsMapToString(defaultOptions));
    assertEquals(
        "connectTimeout=10&socketTimeout=0&autoReconnect=true&useUnicode=true&characterEncoding=utf8",
        AccountSQLStorage.optionsMapToString(modifiedOptions));
  }

  @Test
  public void connectionTest() {
    try {
      new AccountSQLStorage(
          "localhost",
          TestDatabase.getPort(),
          database,
          username,
          password,
          tablePrefix,
          defaultOptions);
    } catch (SQLException e) {
      fail("No SQL exception expected: " + e.getLocalizedMessage());
    }
  }

  @Test(expected = SQLException.class)
  public void exceptionsTest() throws SQLException {
    new AccountSQLStorage(
        "example.com",
        1,
        "example.com",
        "example.com",
        "example.com",
        "",
        "connectTimeout=10&socketTimeout=10");
  }

  @Test
  public void loadAndSaveTest() {
    try {
      ProxyChatAccountStorage accountStorage =
          new AccountSQLStorage(
              "localhost",
              TestDatabase.getPort(),
              database,
              username,
              password,
              tablePrefix,
              defaultOptions);
      UUID testUUID = UUID.randomUUID();

      AccountInfo accountInfo = accountStorage.load(testUUID);
      ProxyChatAccount account = accountInfo.getAccount();

      assertTrue("Should be new account", accountInfo.isNewAccount());

      account.setChannelType(ChannelType.LOCAL);
      account.addIgnore(account);

      accountStorage.save(account);

      AccountInfo accountInfo2 = accountStorage.load(testUUID);
      ProxyChatAccount account2 = accountInfo.getAccount();

      assertFalse("Should not be new account", accountInfo2.isNewAccount());
      Assert.assertEquals(
          "Should be same channel type", account.getChannelType(), account2.getChannelType());
      assertTrue("Should ignore itself", account2.hasIgnored(testUUID));
    } catch (SQLException e) {
      fail("No SQL exception expected: " + e.getLocalizedMessage());
    }
  }
}
