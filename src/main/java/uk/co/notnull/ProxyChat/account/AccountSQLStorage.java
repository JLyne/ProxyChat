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

import com.google.common.annotations.VisibleForTesting;
import uk.co.notnull.ProxyChat.api.account.AccountInfo;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccountStorage;
import uk.co.notnull.ProxyChat.api.enums.ChannelType;
import uk.co.notnull.ProxyChat.util.LoggerHelper;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import lombok.NonNull;

public class AccountSQLStorage implements ProxyChatAccountStorage {
  private final Connection connection;
  private final String tablePrefix;

  private final String tableAccounts;
  private final String tableAccountsColumnUUID;
  private final String tableAccountsColumnUserName;
  private final String tableAccountsColumnChannelType;
  private final String tableAccountsColumnVanished;
  private final String tableAccountsColumnMessenger;
  private final String tableAccountsColumnSocialSpy;
  private final String tableAccountsColumnLocalSpy;
  private final String tableAccountsColumnMutedUntil;
  private final String tableIgnores;
  private final String tableIgnoresColumnUser;
  private final String tableIgnoresColumnIgnores;

  @NonNull private PreparedStatement saveAccount;
  @NonNull private PreparedStatement loadAccount;
  @NonNull private PreparedStatement deleteIgnores;
  @NonNull private PreparedStatement addIgnore;
  @NonNull private PreparedStatement getIgnores;

  private static byte[] getBytesFromUUID(UUID uuid) {
    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());

    return bb.array();
  }

  private static UUID getUUIDFromBytes(byte[] bytes) {
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    long mostSigBits = bb.getLong();
    long leastSigBits = bb.getLong();

    return new UUID(mostSigBits, leastSigBits);
  }

  @VisibleForTesting
  static String optionsMapToString(Map<String, String> options) {
    final Map<String, String> allOptions = new LinkedHashMap<>();
    allOptions.put("connectTimeout", "0");
    allOptions.put("socketTimeout", "0");
    allOptions.put("autoReconnect", "true");
    allOptions.putAll(options);

    return allOptions.entrySet().stream()
        .map(entry -> urlEncode(entry.getKey()) + '=' + urlEncode(entry.getValue()))
        .collect(Collectors.joining("&"));
  }

  private static String urlEncode(String message) {
	 try {
		return URLEncoder.encode(message, StandardCharsets.UTF_8.name());
	 } catch (final UnsupportedEncodingException $ex) {
		  throw lombok.Lombok.sneakyThrow($ex);
	  }
  }

  public AccountSQLStorage(
      String ip,
      int port,
      String database,
      String username,
      String password,
      String tablePrefix,
      Map<String, String> options)
      throws SQLException {
    this(ip, port, database, username, password, tablePrefix, optionsMapToString(options));
  }

  public AccountSQLStorage(
      String ip,
      int port,
      String database,
      String username,
      String password,
      String tablePrefix,
      String options)
      throws SQLException {
    this.tablePrefix = tablePrefix;

    tableAccounts = getTableName("Accounts");
    tableAccountsColumnUUID = "UUID";
    tableAccountsColumnUserName = "UserName";
    tableAccountsColumnChannelType = "ChannelType";
    tableAccountsColumnVanished = "Vanished";
    tableAccountsColumnMessenger = "Messenger";
    tableAccountsColumnSocialSpy = "SocialSpy";
    tableAccountsColumnLocalSpy = "LocalSpy";
    tableAccountsColumnMutedUntil = "MutedUntil";
    tableIgnores = getTableName("Ignores");
    tableIgnoresColumnUser = "User";
    tableIgnoresColumnIgnores = "Ignores";

    String host =
        "jdbc:mysql://"
            + ip
            + ":"
            + port
            + "/"
            + database
            + (options.isEmpty() ? "" : ('?' + options));

    connection = DriverManager.getConnection(host, username, password);

    prepareTables();
    prepareStatements();
  }

	@Override
	public void save(ProxyChatAccount account) {
		try {
			try {
				byte[] uuidBytes = getBytesFromUUID(account.getUniqueId());
				// deleteIgnores
				deleteIgnores.setBytes(1, uuidBytes);
				deleteIgnores.execute();
				deleteIgnores.clearParameters();
				// saveAccount
				saveAccount.setBytes(1, uuidBytes);
				saveAccount.setString(2, account.getName());
				saveAccount.setString(3, account.getChannelType().name());
				saveAccount.setBoolean(4, account.isVanished());
				saveAccount.setBoolean(5, account.hasMessangerEnabled());
				saveAccount.setBoolean(6, account.hasSocialSpyEnabled());
				saveAccount.setBoolean(7, account.hasLocalSpyEnabled());
				saveAccount.setTimestamp(8, account.getMutedUntil());
				saveAccount.executeUpdate();
				saveAccount.clearParameters();
				// addIgnore
				addIgnore.setBytes(1, uuidBytes);
				for (UUID uuid : account.getIgnored()) {
					addIgnore.setBytes(2, getBytesFromUUID(uuid));
					addIgnore.executeUpdate();
				}
				addIgnore.clearParameters();
			} catch (SQLException e) {
				LoggerHelper.error("Could not save user " + account.getUniqueId() + " to database!", e);
			}
		} catch (final java.lang.Throwable $ex) {
			throw lombok.Lombok.sneakyThrow($ex);
		}
	}

	@Override
	public AccountInfo load(UUID uuid) {
		try {
			try {
				byte[] uuidBytes = getBytesFromUUID(uuid);
				// loadAccount
				loadAccount.setBytes(1, uuidBytes);
				try (ResultSet resultLoadAccount = loadAccount.executeQuery()) {
					loadAccount.clearParameters();
					if (!resultLoadAccount.next()) return new AccountInfo(new Account(uuid), true, true);
					// getIgnores
					getIgnores.setBytes(1, uuidBytes);
					try (ResultSet resultGetIgnores = getIgnores.executeQuery()) {
						getIgnores.clearParameters();
						BlockingQueue<UUID> ignores = new LinkedBlockingQueue<>();
						while (resultGetIgnores.next()) {
							ignores.add(getUUIDFromBytes(resultGetIgnores.getBytes(tableIgnoresColumnIgnores)));
						}
						return new AccountInfo(new Account(uuid, ChannelType.valueOf(resultLoadAccount.getString(tableAccountsColumnChannelType)), resultLoadAccount.getBoolean(tableAccountsColumnVanished), resultLoadAccount.getBoolean(tableAccountsColumnMessenger), resultLoadAccount.getBoolean(tableAccountsColumnSocialSpy), resultLoadAccount.getBoolean(tableAccountsColumnLocalSpy), ignores, resultLoadAccount.getTimestamp(tableAccountsColumnMutedUntil)), true, false);
					}
				}
			} catch (SQLException e) {
				LoggerHelper.error("Could not load user " + uuid + " from database!", e);
				return new AccountInfo(new Account(uuid), true, true);
			}
		} catch (final java.lang.Throwable $ex) {
			throw lombok.Lombok.sneakyThrow($ex);
		}
	}

  @Override
  public boolean requiresConsoleAccountSave() {
    return true;
  }

  private boolean isConnectionActive() {
    try {
      return (connection != null) && connection.isValid(0);
    } catch (SQLException e) {
      e.printStackTrace();

      return false;
    }
  }

  private Statement getStatement() throws SQLException {
    if (!isConnectionActive()) throw new SQLException("MySQL-connection is not active!");

    return connection.createStatement();
  }

  private PreparedStatement getPreparedStatement(final String statement) throws SQLException {
    return connection.prepareStatement(statement);
  }

	@SuppressWarnings("unused")
	private ResultSet executeQuery(final String query) throws SQLException {
		Statement statement = getStatement();
		try {
			return statement.executeQuery(query);
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	private boolean executeStatement(final String query) throws SQLException {
		Statement statement = getStatement();
		try {
			return statement.execute(query);
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	@SuppressWarnings("unused")
	private int executeUpdate(final String query) throws SQLException {
		Statement statement = getStatement();
		try {
			return statement.executeUpdate(query);
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

  private String getTableName(String baseName) {
    String name = tablePrefix + baseName;
    name = '`' + name.replaceAll("`", "``") + '`';

    return name;
  }

  private void prepareTables() {
    try {
      final String channelTypeEnum =
          Arrays.stream(ChannelType.values())
              .map(ChannelType::name)
              .collect(Collectors.joining("','", " ENUM('", "')"));

      final String createAccountsTable =
          "CREATE TABLE IF NOT EXISTS "
              + tableAccounts
              + " ("
              + tableAccountsColumnUUID
              + " BINARY(16) NOT NULL, "
              + tableAccountsColumnUserName
              + " VARCHAR(16) NOT NULL, "
              + tableAccountsColumnChannelType
              + channelTypeEnum
              + " NOT NULL, "
              + tableAccountsColumnVanished
              + " BOOLEAN NOT NULL, "
              + tableAccountsColumnMessenger
              + " BOOLEAN NOT NULL, "
              + tableAccountsColumnSocialSpy
              + " BOOLEAN NOT NULL, "
              + tableAccountsColumnLocalSpy
              + " BOOLEAN NOT NULL, "
              + tableAccountsColumnMutedUntil
              + " DATETIME NOT NULL, PRIMARY KEY ("
              + tableAccountsColumnUUID
              + ")) DEFAULT CHARSET=utf8";
      final String createIgnoresTable =
          "CREATE TABLE IF NOT EXISTS "
              + tableIgnores
              + " ("
              + tableIgnoresColumnUser
              + " BINARY(16) NOT NULL, "
              + tableIgnoresColumnIgnores
              + " BINARY(16) NOT NULL, PRIMARY KEY ("
              + tableIgnoresColumnUser
              + ", "
              + tableIgnoresColumnIgnores
              + "), KEY ("
              + tableIgnoresColumnUser
              + "), KEY ("
              + tableIgnoresColumnIgnores
              + "), CONSTRAINT FOREIGN KEY ("
              + tableIgnoresColumnUser
              + ") REFERENCES "
              + tableAccounts
              + " ("
              + tableAccountsColumnUUID
              + "), CONSTRAINT FOREIGN KEY ("
              + tableIgnoresColumnIgnores
              + ") REFERENCES "
              + tableAccounts
              + " ("
              + tableAccountsColumnUUID
              + ")) DEFAULT CHARSET=utf8";

      executeStatement(createAccountsTable);
      executeStatement(createIgnoresTable);
    } catch (SQLException e) {
      LoggerHelper.error("Could not create tables!", e);
    }
  }

  private void prepareStatements() {
    try {
      final String saveAccountStr =
          "INSERT INTO "
              + tableAccounts
              + " ("
              + tableAccountsColumnUUID
              + ", "
              + tableAccountsColumnUserName
              + ", "
              + tableAccountsColumnChannelType
              + ", "
              + tableAccountsColumnVanished
              + ", "
              + tableAccountsColumnMessenger
              + ", "
              + tableAccountsColumnSocialSpy
              + ", "
              + tableAccountsColumnLocalSpy
              + ", "
              + tableAccountsColumnMutedUntil
              + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
              + tableAccountsColumnUserName
              + " = VALUES("
              + tableAccountsColumnUserName
              + "), "
              + tableAccountsColumnChannelType
              + " = VALUES("
              + tableAccountsColumnChannelType
              + "), "
              + tableAccountsColumnVanished
              + " = VALUES("
              + tableAccountsColumnVanished
              + "), "
              + tableAccountsColumnMessenger
              + " = VALUES("
              + tableAccountsColumnMessenger
              + "), "
              + tableAccountsColumnSocialSpy
              + " = VALUES("
              + tableAccountsColumnSocialSpy
              + "), "
              + tableAccountsColumnLocalSpy
              + " = VALUES("
              + tableAccountsColumnLocalSpy
              + "), "
              + tableAccountsColumnMutedUntil
              + " = VALUES("
              + tableAccountsColumnMutedUntil
              + ")";
      final String loadAccountStr =
          "SELECT "
              + tableAccountsColumnChannelType
              + ", "
              + tableAccountsColumnVanished
              + ", "
              + tableAccountsColumnMessenger
              + ", "
              + tableAccountsColumnSocialSpy
              + ", "
              + tableAccountsColumnLocalSpy
              + ", "
              + tableAccountsColumnMutedUntil
              + " FROM "
              + tableAccounts
              + " WHERE "
              + tableAccountsColumnUUID
              + " = ? LIMIT 1";
      final String deleteIgnoresStr =
          "DELETE FROM " + tableIgnores + " WHERE " + tableIgnoresColumnUser + " = ?";
      final String addIgnoreStr =
          "INSERT INTO "
              + tableIgnores
              + " ("
              + tableIgnoresColumnUser
              + ", "
              + tableIgnoresColumnIgnores
              + ") VALUES (?, ?)";
      final String getIgnoresStr =
          "SELECT "
              + tableIgnoresColumnIgnores
              + " FROM "
              + tableIgnores
              + " WHERE "
              + tableIgnoresColumnUser
              + " = ? ";

      saveAccount = getPreparedStatement(saveAccountStr);
      loadAccount = getPreparedStatement(loadAccountStr);
      deleteIgnores = getPreparedStatement(deleteIgnoresStr);
      addIgnore = getPreparedStatement(addIgnoreStr);
      getIgnores = getPreparedStatement(getIgnoresStr);
    } catch (SQLException e) {
      LoggerHelper.error("Could not prepare statements!", e);
    }
  }
}
