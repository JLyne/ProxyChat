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

package uk.co.notnull.ProxyChat.api.account;

public final class AccountInfo {
  private final ProxyChatAccount account;
  private final boolean forceSave;
  private final boolean newAccount;

  public AccountInfo(final ProxyChatAccount account, final boolean forceSave, final boolean newAccount) {
    this.account = account;
    this.forceSave = forceSave;
    this.newAccount = newAccount;
  }

  public ProxyChatAccount getAccount() {
    return this.account;
  }

  public boolean isForceSave() {
    return this.forceSave;
  }

  public boolean isNewAccount() {
    return this.newAccount;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof AccountInfo)) return false;
    final AccountInfo other = (AccountInfo) o;
    if (this.isForceSave() != other.isForceSave()) return false;
    if (this.isNewAccount() != other.isNewAccount()) return false;
    final Object this$account = this.getAccount();
    final Object other$account = other.getAccount();
    if (this$account == null ? other$account != null : !this$account.equals(other$account)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + (this.isForceSave() ? 79 : 97);
    result = result * PRIME + (this.isNewAccount() ? 79 : 97);
    final Object $account = this.getAccount();
    result = result * PRIME + ($account == null ? 43 : $account.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "AccountInfo(account=" + this.getAccount() + ", forceSave=" + this.isForceSave() + ", newAccount=" + this.isNewAccount() + ")";
  }
}
