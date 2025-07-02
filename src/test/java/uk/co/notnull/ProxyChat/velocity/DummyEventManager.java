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

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.PostOrder;

import java.util.concurrent.CompletableFuture;

public class DummyEventManager implements EventManager {
	@Override
	public void register(Object o, Object o1) {

	}

	@Override
	public <E> void register(Object o, Class<E> aClass, PostOrder postOrder, EventHandler<E> eventHandler) {

	}

	@Override
	public <E> void register(Object o, Class<E> aClass, short i, EventHandler<E> eventHandler) {

	}

	@Override
	public <E> CompletableFuture<E> fire(E e) {
		return CompletableFuture.completedFuture(e);
	}

	@Override
	public void unregisterListeners(Object o) {

	}

	@Override
	public void unregisterListener(Object o, Object o1) {

	}

	@Override
	public <E> void unregister(Object o, EventHandler<E> eventHandler) {

	}
}
