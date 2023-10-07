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

import uk.co.notnull.ProxyChat.api.ProxyChatApi;
import uk.co.notnull.ProxyChat.api.account.AccountInfo;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccountStorage;
import uk.co.notnull.ProxyChat.api.enums.ChannelType;
import uk.co.notnull.ProxyChat.util.LoggerHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class AccountFileStorage implements ProxyChatAccountStorage {
  private static final String FILE_EXTENSION = ".sav";

  private static File getUserDataDir() throws IOException {
    File folder = new File(ProxyChatApi.getInstance().getConfigFolder(), "userdata");

    if (!folder.exists() && !folder.mkdirs()) throw new IOException("Could not create " + folder);

    return folder;
  }

  @Override
  public void save(ProxyChatAccount account) {
    try {
      File accountFile = new File(getUserDataDir(), account.getUniqueId() + FILE_EXTENSION);

      if (!accountFile.exists() && !accountFile.createNewFile()) {
        throw new IOException("Could not create " + accountFile);
      }

      FileOutputStream saveFile = new FileOutputStream(accountFile);
      try {
        ObjectOutputStream save = new ObjectOutputStream(saveFile);
        try {
          save.writeObject(account.getName());
          save.writeObject(account.getChannelType());
          save.writeObject(account.isVanished());
          save.writeObject(account.hasMessangerEnabled());
          save.writeObject(account.hasSocialSpyEnabled());
          save.writeObject(account.hasLocalSpyEnabled());
          save.writeObject(account.getIgnored());
          save.writeObject(account.getMutedUntil());
        } finally {
          if (save != null) {
            save.close();
          }
        }
      } finally {
        if (saveFile != null) {
          saveFile.close();
        }
      }
    } catch (IOException e) {
      LoggerHelper.warning("Could not save player " + account.getUniqueId(), e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public AccountInfo load(UUID uuid) {
    try {
      File accountFile = new File(getUserDataDir(), uuid.toString() + FILE_EXTENSION);

      if (!accountFile.exists()) return new AccountInfo(new Account(uuid), false, true);

      FileInputStream saveFile = new FileInputStream(accountFile);
      try {
        ObjectInputStream save = new ObjectInputStream(saveFile);
        try {
          // Read Name (and discard it (for now))
          save.readObject();

          return new AccountInfo(
                  new Account(
                          uuid,
                          (ChannelType) save.readObject(),
                          (boolean) save.readObject(),
                          (boolean) save.readObject(),
                          (boolean) save.readObject(),
                          (boolean) save.readObject(),
                          (BlockingQueue<UUID>) save.readObject(),
                          (Timestamp) save.readObject()),
                  false,
                  false);
        } finally {
          if (save != null) {
            save.close();
          }
        }
      } finally {
        if (saveFile != null) {
          saveFile.close();
        }
      }
    } catch (IOException | ClassNotFoundException | ClassCastException e) {
      LoggerHelper.warning("Could not load player " + uuid, e);

      return new AccountInfo(new Account(uuid), false, true);
    }
  }
}
