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

package uk.co.notnull.ProxyChat.hook;

import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;
import uk.co.notnull.ProxyChat.api.hook.ProxyChatHook;
import uk.co.notnull.ProxyChat.api.hook.HookManager;
import java.util.Objects;
import java.util.Optional;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.context.ContextManager;
import net.luckperms.api.context.DefaultContextKeys;
import net.luckperms.api.context.MutableContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;

public class LuckPerms5Hook implements ProxyChatHook {
  private final boolean fixContexts;
  private final LuckPerms api = LuckPermsProvider.get();

  @Override
  public Optional<String> getPrefix(ProxyChatAccount account) {
    return getMetaData(account).map(CachedMetaData::getPrefix);
  }

  @Override
  public Optional<String> getSuffix(ProxyChatAccount account) {
    return getMetaData(account).map(CachedMetaData::getSuffix);
  }

  private Optional<CachedMetaData> getMetaData(ProxyChatAccount account) {
    final Optional<User> user =
        Optional.ofNullable(api.getUserManager().getUser(account.getUniqueId()));

    return user.map(User::getCachedData)
        .map(data -> data.getMetaData(getQueryOptions(user)))
        .filter(Objects::nonNull);
  }

  @Override
  public int getPriority() {
    return HookManager.PERMISSION_PLUGIN_PREFIX_PRIORITY;
  }

  private QueryOptions getQueryOptions(Optional<User> user) {
    final ContextManager contextManager = api.getContextManager();
    final QueryOptions queryOptions =
        user.flatMap(contextManager::getQueryOptions)
            .orElseGet(contextManager::getStaticQueryOptions);

    if (fixContexts && (queryOptions.mode() == QueryMode.CONTEXTUAL)) {
      final MutableContextSet context = queryOptions.context().mutableCopy();

      context
          .getValues(DefaultContextKeys.WORLD_KEY)
          .forEach(world -> context.add(DefaultContextKeys.SERVER_KEY, world));

      return queryOptions.toBuilder().context(context).build();
    } else {
      return queryOptions;
    }
  }

  public LuckPerms5Hook(final boolean fixContexts) {
    this.fixContexts = fixContexts;
  }
}
