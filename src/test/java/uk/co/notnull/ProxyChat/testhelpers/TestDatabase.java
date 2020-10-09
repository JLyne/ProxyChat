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

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfiguration;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

@UtilityClass
public class TestDatabase {
  private static final String baseDir = SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/base/";
  private static final String localhost = "localhost";
  private static DB databaseInstance;
  @Getter private static String host;
  @Getter private static int port;

  @SneakyThrows(ManagedProcessException.class)
  public static void startDatabase() {
    final int limit = 100;
    int count = 0;
    String actualBaseDir;
    String actualDataDir;

    do {
      actualBaseDir = baseDir + count;
    } while ((++count < limit) && (new File(actualBaseDir)).exists());

    Preconditions.checkElementIndex(count, limit, "count must be less than " + limit);

    actualDataDir = actualBaseDir + "/data";
    final DBConfiguration config =
        DBConfigurationBuilder.newBuilder()
            .setPort(0)
            .setSocket(localhost)
            .setBaseDir(actualBaseDir)
            .setDataDir(actualDataDir)
            .build();
    databaseInstance = DB.newEmbeddedDB(config);
    databaseInstance.start();

    port = databaseInstance.getConfiguration().getPort();
    host = localhost + ':' + port;

    databaseInstance.createDB("test");
  }

  @SneakyThrows({ManagedProcessException.class})
  public static void stopDatabase() {
    databaseInstance.stop();

    try {
      Thread.sleep(500);

      FileUtils.deleteDirectory(new File(databaseInstance.getConfiguration().getBaseDir()));
    } catch (IOException | InterruptedException e) {
      // Ignore
    }
  }

  public static Connection getDatabaseInstance() throws SQLException {
    return DriverManager.getConnection("jdbc:mysql://" + host + "/test", "test", "test");
  }

  public static void closeDatabaseInstance(Connection database) throws SQLException {
    database.close();
  }
}
