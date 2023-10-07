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

package uk.co.notnull.ProxyChat.testhelpers;

import com.velocitypowered.api.command.CommandSource;
import uk.co.notnull.ProxyChat.account.ProxyChatAccountManager;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.enums.AccountType;
import java.util.UUID;
import org.junit.BeforeClass;
import org.mockito.Mockito;

public abstract class AccountManagerTest {
  protected static final CommandSource console = Mockito.mock(CommandSource.class);

  @BeforeClass
  public static void setupAccounts() {
    HelperAccountManager.clearPlayers();
    HelperAccountManager.addPlayer("test");
    HelperAccountManager.addPlayer("player1");
    HelperAccountManager.addPlayer("player2");
    HelperAccountManager.addPlayer("hello");

    Mockito.when(console.hasPermission(Mockito.any())).thenReturn(true);
  }


  private static final class HelperAccountManager extends ProxyChatAccountManager {
    private static long id = 0;

    public static void addPlayer(String name) {
      final UUID uuid = new UUID(0, id++);
      final ProxyChatAccount account = Mockito.mock(ProxyChatAccount.class);

      Mockito.when(account.getUniqueId()).thenReturn(uuid);
      Mockito.when(account.getName()).thenReturn(name);
      Mockito.when(account.getAccountType()).thenReturn(AccountType.PLAYER);

      accounts.put(uuid, account);
    }

    public static void clearPlayers() {
      accounts.clear();
      id = 0;
    }

    private HelperAccountManager() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
  }
}
