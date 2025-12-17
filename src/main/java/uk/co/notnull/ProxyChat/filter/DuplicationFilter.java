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

package uk.co.notnull.ProxyChat.filter;

import com.google.common.annotations.VisibleForTesting;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.filter.BlockMessageException;
import uk.co.notnull.ProxyChat.api.filter.ProxyChatPreParseFilter;
import uk.co.notnull.ProxyChat.api.filter.FilterManager;
import uk.co.notnull.ProxyChat.message.Messages;
import uk.co.notnull.ProxyChat.api.permission.Permission;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class DuplicationFilter implements ProxyChatPreParseFilter {
  private final ConcurrentMap<UUID, Queue<TimePointMessage>> playerMessagesStorage;
  private final int checkPastMessages;
  private final long expiryTimer;
  private final boolean noPermissions;

  public DuplicationFilter(int checkPastMessages, int expireAfter) {
    this(checkPastMessages, expireAfter, false);
  }

  @VisibleForTesting
  DuplicationFilter(int checkPastMessages, int expireAfter, boolean noPermissions) {
    playerMessagesStorage = new ConcurrentHashMap<>();
    this.checkPastMessages = checkPastMessages;
    expiryTimer = TimeUnit.SECONDS.toNanos(expireAfter);
    this.noPermissions = noPermissions;
  }

  @Override
  public String applyFilter(ProxyChatAccount sender, String message) throws BlockMessageException {
    if (!noPermissions && sender.hasPermission(Permission.BYPASS_ANTI_DUPLICATE))
      return message;

    final UUID uuid = sender.getUniqueId();

    if (!playerMessagesStorage.containsKey(uuid)) {
      playerMessagesStorage.put(uuid, new ArrayDeque<>(checkPastMessages));
    }

    final Queue<TimePointMessage> playerMessages = playerMessagesStorage.get(uuid);
    final long now = System.nanoTime();
    final long expiry = now - expiryTimer;

    while (!playerMessages.isEmpty() && (playerMessages.peek().getTimePoint() < expiry)) {
      playerMessages.poll();
    }

    if (playerMessages.stream().map(TimePointMessage::getMessage).anyMatch(message::equals))
      throw new ExtendedBlockMessageException(Messages.ANTI_DUPLICATION, sender);

    if (playerMessages.size() == checkPastMessages) {
      playerMessages.remove();
    }

    playerMessages.add(new TimePointMessage(now, message));

    return message;
  }

  @Override
  public int getPriority() {
    return FilterManager.DUPLICATION_FILTER_PRIORITY;
  }

  @VisibleForTesting
  void clear() {
    playerMessagesStorage.clear();
  }


  private static final class TimePointMessage {
    private final long timePoint;
    private final String message;

    public TimePointMessage(final long timePoint, final String message) {
      this.timePoint = timePoint;
      this.message = message;
    }

    public long getTimePoint() {
      return this.timePoint;
    }

    public String getMessage() {
      return this.message;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof TimePointMessage other)) return false;
      if (this.getTimePoint() != other.getTimePoint()) return false;
      final Object this$message = this.getMessage();
      final Object other$message = other.getMessage();
      return Objects.equals(this$message, other$message);
	}

    @Override
    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final long $timePoint = this.getTimePoint();
      result = result * PRIME + Long.hashCode($timePoint);
      final Object $message = this.getMessage();
      result = result * PRIME + ($message == null ? 43 : $message.hashCode());
      return result;
    }

    @Override
    public String toString() {
      return "DuplicationFilter.TimePointMessage(timePoint=" + this.getTimePoint() + ", message=" + this.getMessage() + ")";
    }
  }
}
