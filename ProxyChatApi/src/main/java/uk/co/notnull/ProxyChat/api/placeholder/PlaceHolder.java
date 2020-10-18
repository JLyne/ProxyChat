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

package uk.co.notnull.ProxyChat.api.placeholder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.kyori.adventure.text.Component;

@EqualsAndHashCode(of = "placeholder")
public class PlaceHolder implements ProxyChatPlaceHolder {
  @Getter private final String placeholder;
  private ReplacementSupplier replacementSupplier;
  private ComponentReplacementSupplier componentReplacementSupplier = context -> Component.text(replacementSupplier.get(context));
  private final List<Predicate<? super ProxyChatContext>> requirements = new LinkedList<>();

  @SafeVarargs
  public PlaceHolder(
      String placeholder,
      ReplacementSupplier replacementSupplier,
      Predicate<? super ProxyChatContext>... requirements) {
    this(placeholder, replacementSupplier, Arrays.asList(requirements));
  }

  @SafeVarargs
  public PlaceHolder(
      String placeholder,
      ReplacementSupplier replacementSupplier,
      ComponentReplacementSupplier componentReplacementSupplier,
      Predicate<? super ProxyChatContext>... requirements) {
    this(placeholder, replacementSupplier, componentReplacementSupplier, Arrays.asList(requirements));
  }

  public PlaceHolder(
      String placeholder,
      ReplacementSupplier replacementSupplier,
      List<Predicate<? super ProxyChatContext>> requirements) {
    this.placeholder = placeholder;
    this.replacementSupplier = replacementSupplier;
    this.requirements.addAll(requirements);
  }

  public PlaceHolder(
      String placeholder,
      ReplacementSupplier replacementSupplier,
      ComponentReplacementSupplier componentReplacementSupplier,
      List<Predicate<? super ProxyChatContext>> requirements) {
    this.placeholder = placeholder;
    this.replacementSupplier = replacementSupplier;
    this.componentReplacementSupplier = componentReplacementSupplier;
    this.requirements.addAll(requirements);
  }


  @Override
  public boolean isContextApplicable(ProxyChatContext context) {
    for (Predicate<? super ProxyChatContext> requirement : requirements) {
      if (!requirement.test(context)) return false;
    }

    return true;
  }

  @Override
  public Component getReplacementComponent(String name, ProxyChatContext context) {
    return componentReplacementSupplier.get(context);
  }

  @Override
  public String getReplacement(String name, ProxyChatContext context) {
    return replacementSupplier.get(context);
  }

  public void addRequirement(Predicate<? super ProxyChatContext> requirement) {
    if (requirements.contains(requirement)) return;

    requirements.add(requirement);
  }

  @Override
  public String getName() {
    return getPlaceholder();
  }

  public PlaceHolder[] createAliases(String... aliases) {
    int size = aliases.length;
    PlaceHolder[] placeHolders = new PlaceHolder[size + 1];

    for (int i = 0; i < size; i++) {
      placeHolders[i] = new PlaceHolder(aliases[i], replacementSupplier, requirements);
    }

    placeHolders[size] = this;

    return placeHolders;
  }
}
