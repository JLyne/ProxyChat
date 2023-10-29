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

package uk.co.notnull.ProxyChat.module;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.velocitypowered.api.scheduler.ScheduledTask;
import uk.co.notnull.ProxyChat.ProxyChat;
import uk.co.notnull.ProxyChat.task.AutomaticBroadcastTask;
import uk.co.notnull.ProxyChat.util.PredicateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AutoBroadcastModule extends Module {
	private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

	private List<ScheduledTask> automaticBroadcastTasks;

	@Override
	public String getName() {
		return "AutoBroadcast";
	}

	@Override
	public void onEnable() {
		automaticBroadcastTasks = new ArrayList<>();
		ConfigList section = getModuleSection().getList("broadcasts");

		section.forEach((ConfigValue broadcast) -> {
			Config broadcastConfig = ((ConfigObject) broadcast).toConfig();

			long interval = broadcastConfig.getDuration("interval", TIME_UNIT);
			long delay = Math.min(10, interval / 2);

			ScheduledTask task = ProxyChat.getInstance().getProxy().getScheduler()
					.buildTask(
							ProxyChat.getInstance(),
							new AutomaticBroadcastTask(
									PredicateUtil.getServerListPredicate(broadcastConfig.getConfig("serverList")),
									broadcastConfig.getStringList("messages"), broadcastConfig.getBoolean("random")))
					.delay(delay, TIME_UNIT)
					.repeat(interval, TIME_UNIT)
					.schedule();

			automaticBroadcastTasks.add(task);
		});
	}

	@Override
	public void onDisable() {
		automaticBroadcastTasks.forEach(ScheduledTask::cancel);
	}
}
