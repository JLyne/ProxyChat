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

package uk.co.notnull.ProxyChat.velocity;

import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DummyScheduler implements Scheduler {
	@Override
	public TaskBuilder buildTask(@NonNull Object o, @NonNull Runnable runnable) {
		TaskBuilder mock = Mockito.mock(TaskBuilder.class);
		Mockito.when(mock.clearDelay()).thenReturn(mock);
		Mockito.when(mock.clearRepeat()).thenReturn(mock);
		Mockito.when(mock.delay(Mockito.any(Duration.class))).thenReturn(mock);
		Mockito.when(mock.delay(Mockito.any(Long.class), Mockito.any(TimeUnit.class))).thenReturn(mock);
		Mockito.when(mock.repeat(Mockito.any(Duration.class))).thenReturn(mock);
		Mockito.when(mock.repeat(Mockito.any(Long.class), Mockito.any(TimeUnit.class))).thenReturn(mock);
		Mockito.when(mock.schedule()).thenReturn(Mockito.mock(ScheduledTask.class));

		return mock;
	}

	@Override
	public TaskBuilder buildTask(@NotNull Object plugin, @NotNull Consumer<ScheduledTask> consumer) {
		return null;
	}

	@Override
	public @NotNull Collection<ScheduledTask> tasksByPlugin(@NotNull Object plugin) {
		return Collections.emptyList();
	}
}
