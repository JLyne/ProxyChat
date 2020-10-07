package dev.aura.bungeechat.module;

import dev.aura.bungeechat.api.filter.FilterManager;
import dev.aura.bungeechat.filter.DuplicationFilter;

public class AntiDuplicationModule extends Module {
  @Override
  public String getName() {
    return "AntiDuplication";
  }

  @Override
  public void onEnable() {
    FilterManager.addPreParseFilter(
        getName(),
        new DuplicationFilter(
            getModuleSection().getInt("checkPastMessages"),
            getModuleSection().getInt("expireAfter")));
  }

  @Override
  public void onDisable() {
    FilterManager.removePreParseFilter(getName());
  }
}
