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

package uk.co.notnull.ProxyChat.chatlog;

import uk.co.notnull.ProxyChat.ProxyChat;
import uk.co.notnull.ProxyChat.api.placeholder.ProxyChatContext;
import uk.co.notnull.ProxyChat.api.placeholder.PlaceHolderManager;
import uk.co.notnull.ProxyChat.message.Format;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class FileLogger implements ChatLogger, AutoCloseable {
  private static final ProxyChatContext context = new ProxyChatContext();
  private static final File pluginDir = ProxyChat.getInstance().getConfigFolder();

  private final String logFile;
  private Writer fw;
  private PrintWriter pw;

  @Override
  public void log(ProxyChatContext context) {
    initLogFile();

    pw.println(Format.CHAT_LOGGING_FILE.getRaw(context));
    pw.flush();
  }

  @Override
  public void close() throws IOException {
    fw.close();
    pw.close();
  }

  private void initLogFile() {
    String newFile = PlaceHolderManager.processMessage(logFile, context);

    String oldFile = "";
    if (oldFile.equals(newFile)) return;

    try {
      File saveTo = new File(pluginDir, newFile);
      Optional.ofNullable(saveTo.getParentFile()).ifPresent(File::mkdirs);

      if (!saveTo.exists() && !saveTo.createNewFile()) {
        throw new IOException("Could not create " + saveTo);
      }

      fw = new OutputStreamWriter(new FileOutputStream(saveTo, true), StandardCharsets.UTF_8);
      pw = new PrintWriter(fw);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public FileLogger(final String logFile) {
    this.logFile = logFile;
  }
}
