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

import com.velocitypowered.api.proxy.server.RegisteredServer;
import uk.co.notnull.ProxyChat.api.account.ProxyChatAccount;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import uk.co.notnull.ProxyChat.api.enums.ChannelType;

/**
 * This class represents a context for a message or other chat related action.<br>
 * It may contain the acting player (sender), the receiver (target), the message and possibly more
 * in the future.
 */
public class ProxyChatContext {
  /**
   * Predefined Predicate to check if a context has a sender.
   *
   * @see ProxyChatContext#require(Predicate...)
   */
  public static final Predicate<ProxyChatContext> HAS_SENDER = ProxyChatContext::hasSender;
  /**
   * Predefined Predicate to check if a context has a target.
   *
   * @see ProxyChatContext#require(Predicate...)
   */
  public static final Predicate<ProxyChatContext> HAS_TARGET = ProxyChatContext::hasTarget;
  /**
   * Predefined Predicate to check if a context has a message.
   *
   * @see ProxyChatContext#require(Predicate...)
   */
  public static final Predicate<ProxyChatContext> HAS_MESSAGE = ProxyChatContext::hasMessage;
  /**
   * Predefined Predicate to check if a context has a channel.
   *
   * @see ProxyChatContext#require(Predicate...)
   */
  public static final Predicate<ProxyChatContext> HAS_CHANNEL = ProxyChatContext::hasChannel;
  /**
   * Predefined Predicate to check if a context has a server.
   *
   * @see ProxyChatContext#require(Predicate...)
   */
  public static final Predicate<ProxyChatContext> HAS_SERVER = ProxyChatContext::hasServer;

  /**
   * Predefined Predicate to check if a context has been filtered.
   *
   * @see ProxyChatContext#require(Predicate...)
   */
  public static final Predicate<ProxyChatContext> IS_FILTERED = ProxyChatContext::isFiltered;

  /**
   * Predefined Predicate to check if a context has been parsed.
   *
   * @see ProxyChatContext#require(Predicate...)
   */
  public static final Predicate<ProxyChatContext> IS_PARSED = ProxyChatContext::isParsed;

  /**
   * Predefined Predicate to check if a context does not have a sender.
   *
   * @see ProxyChatContext#require(Predicate...)
   */
  public static final Predicate<ProxyChatContext> HAS_NO_SENDER = HAS_SENDER.negate();
  /**
   * Predefined Predicate to check if a context does not have a target.
   *
   * @see ProxyChatContext#require(Predicate...)
   */
  public static final Predicate<ProxyChatContext> HAS_NO_TARGET = HAS_TARGET.negate();
  /**
   * Predefined Predicate to check if a context does not have a message.
   *
   * @see ProxyChatContext#require(Predicate...)
   */
  public static final Predicate<ProxyChatContext> HAS_NO_MESSAGE = HAS_MESSAGE.negate();
  /**
   * Predefined Predicate to check if a context does not have a channel.
   *
   * @see ProxyChatContext#require(Predicate...)
   */
  public static final Predicate<ProxyChatContext> HAS_NO_CHANNEL = HAS_CHANNEL.negate();
  /**
   * Predefined Predicate to check if a context does not have a server.
   *
   * @see ProxyChatContext#require(Predicate...)
   */
  public static final Predicate<ProxyChatContext> HAS_NO_SERVER = HAS_SERVER.negate();

  private static final Map<Predicate<ProxyChatContext>, String> requirementsNameCache =
      new HashMap<>(8);

  private ProxyChatAccount sender;
  private ProxyChatAccount target;
  private String message;
  private String filteredMessage;
  private Component parsedMessage;
  private ChannelType channel;
  private RegisteredServer server;
  private boolean parsed = false;
  private boolean filtered = false;

  public ProxyChatContext() {
    sender = null;
    target = null;
    message = null;
    filteredMessage = null;
    parsedMessage = null;
    channel = null;
    server = null;
  }

  public ProxyChatContext(ProxyChatAccount sender) {
    this();

    this.sender = sender;
  }

  public ProxyChatContext(String message) {
    this();

    this.message = message;
  }

  public ProxyChatContext(ProxyChatAccount sender, String message) {
    this(sender);
    this.message = message;
  }

  public ProxyChatContext(ProxyChatAccount sender, ProxyChatAccount target) {
    this(sender);

    this.target = target;
  }

  public ProxyChatContext(ProxyChatAccount sender, ProxyChatAccount target, String message) {
    this(sender, target);

    this.message = message;
  }

  public ProxyChatContext(ProxyChatAccount sender, String message, RegisteredServer server) {
    this(sender, message);

    this.server = server;
  }

  /**
   * This method is used to verify if a context is valid. All passed requirements must be true in
   * order for this test to pass. If it fails an {@link InvalidContextError} is thrown.<br>
   * It is recommended to use the static predefined {@link Predicate}s like {@link
   * ProxyChatContext#HAS_SENDER}.
   *
   * @param requirements An array of requirements which all must be true for this context to be
   *     valid.
   * @throws InvalidContextError This assertion error gets thrown when one (or more) requirements
   *     are not met. If it is a predefined {@link Predicate} from {@link ProxyChatContext} the
   *     name will be included in the error message. If not a generic message will be put.
   * @see ProxyChatContext#HAS_SENDER
   * @see ProxyChatContext#HAS_TARGET
   * @see ProxyChatContext#HAS_MESSAGE
   * @see ProxyChatContext#HAS_CHANNEL
   * @see ProxyChatContext#HAS_NO_SENDER
   * @see ProxyChatContext#HAS_NO_TARGET
   * @see ProxyChatContext#HAS_NO_MESSAGE
   * @see ProxyChatContext#HAS_NO_CHANNEL
   */
  @SafeVarargs
  public final void require(Predicate<? super ProxyChatContext>... requirements)
      throws InvalidContextError {
    for (Predicate<? super ProxyChatContext> requirement : requirements) {
      if (!requirement.test(this)) {
        if (requirementsNameCache.containsKey(requirement))
          throw new InvalidContextError(requirementsNameCache.get(requirement));

        throw new InvalidContextError();
      }
    }
  }

  public boolean hasSender() {
    return sender != null;
  }

  public boolean hasTarget() {
    return target != null;
  }

  public boolean hasMessage() {
    return message != null;
  }

  public boolean hasChannel() {
    return channel != null;
  }

  public boolean hasServer() {
    return server != null;
  }

  public Optional<ProxyChatAccount> getSender() {
    return Optional.ofNullable(sender);
  }

  public Optional<ProxyChatAccount> getTarget() {
    return Optional.ofNullable(target);
  }

  public Optional<String> getMessage() {
    return Optional.ofNullable(message);
  }

  public Optional<String> getFilteredMessage() {
    return Optional.ofNullable(filteredMessage);
  }

  public Optional<Component> getParsedMessage() {
    return Optional.ofNullable(parsedMessage);
  }

  public Optional<ChannelType> getChannel() {
    return Optional.ofNullable(channel);
  }

  public Optional<RegisteredServer> getServer() {
    return Optional.ofNullable(server);
  }

  public void setFilteredMessage(String message) {
    filtered = true;
    filteredMessage = message;
  }

  public void setParsedMessage(Component message) {
    parsed = true;
    parsedMessage = message;
  }

  // Fill the requirementsNameCache
  static {
    final int modifers = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;

    for (Field field : ProxyChatContext.class.getDeclaredFields()) {
      try {
        if ((field.getModifiers() & modifers) == modifers) {
          @SuppressWarnings("unchecked")
          Predicate<ProxyChatContext> filter = (Predicate<ProxyChatContext>) field.get(null);

          requirementsNameCache.put(
              filter, "Context does not meet requirement " + field.getName() + "!");
        }

      } catch (IllegalArgumentException | IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

  public boolean isParsed() {
    return this.parsed;
  }

  public boolean isFiltered() {
    return this.filtered;
  }

  public void setSender(final ProxyChatAccount sender) {
    this.sender = sender;
  }

  public void setTarget(final ProxyChatAccount target) {
    this.target = target;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public void setChannel(final ChannelType channel) {
    this.channel = channel;
  }

  public void setServer(final RegisteredServer server) {
    this.server = server;
  }

  public void setParsed(final boolean parsed) {
    this.parsed = parsed;
  }

  public void setFiltered(final boolean filtered) {
    this.filtered = filtered;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ProxyChatContext other)) return false;
    if (!other.canEqual(this)) return false;
    if (this.isParsed() != other.isParsed()) return false;
    if (this.isFiltered() != other.isFiltered()) return false;
    final Object this$sender = this.getSender();
    final Object other$sender = other.getSender();
    if (!Objects.equals(this$sender, other$sender)) return false;
    final Object this$target = this.getTarget();
    final Object other$target = other.getTarget();
    if (!Objects.equals(this$target, other$target)) return false;
    final Object this$message = this.getMessage();
    final Object other$message = other.getMessage();
    if (!Objects.equals(this$message, other$message)) return false;
    final Object this$filteredMessage = this.getFilteredMessage();
    final Object other$filteredMessage = other.getFilteredMessage();
    if (!Objects.equals(this$filteredMessage, other$filteredMessage)) return false;
    final Object this$parsedMessage = this.getParsedMessage();
    final Object other$parsedMessage = other.getParsedMessage();
    if (!Objects.equals(this$parsedMessage, other$parsedMessage)) return false;
    final Object this$channel = this.getChannel();
    final Object other$channel = other.getChannel();
    if (!Objects.equals(this$channel, other$channel)) return false;
    final Object this$server = this.getServer();
    final Object other$server = other.getServer();
    return Objects.equals(this$server, other$server);
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ProxyChatContext;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + (this.isParsed() ? 79 : 97);
    result = result * PRIME + (this.isFiltered() ? 79 : 97);
    final Object $sender = this.getSender();
    result = result * PRIME + ($sender == null ? 43 : $sender.hashCode());
    final Object $target = this.getTarget();
    result = result * PRIME + ($target == null ? 43 : $target.hashCode());
    final Object $message = this.getMessage();
    result = result * PRIME + ($message == null ? 43 : $message.hashCode());
    final Object $filteredMessage = this.getFilteredMessage();
    result = result * PRIME + ($filteredMessage == null ? 43 : $filteredMessage.hashCode());
    final Object $parsedMessage = this.getParsedMessage();
    result = result * PRIME + ($parsedMessage == null ? 43 : $parsedMessage.hashCode());
    final Object $channel = this.getChannel();
    result = result * PRIME + ($channel == null ? 43 : $channel.hashCode());
    final Object $server = this.getServer();
    result = result * PRIME + ($server == null ? 43 : $server.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "ProxyChatContext(sender=" + this.getSender() + ", target=" + this.getTarget() + ", message=" + this.getMessage() + ", filteredMessage=" + this.getFilteredMessage() + ", parsedMessage=" + this.getParsedMessage() + ", channel=" + this.getChannel() + ", server=" + this.getServer() + ", parsed=" + this.isParsed() + ", filtered=" + this.isFiltered() + ")";
  }
}
