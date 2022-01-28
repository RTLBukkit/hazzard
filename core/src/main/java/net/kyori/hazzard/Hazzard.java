/*
 * hazzard - A localisation library for Java.
 * Copyright (C) Mariell Hoversholm
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.kyori.hazzard;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import net.kyori.hazzard.annotation.meta.ThreadSafe;
import net.kyori.hazzard.exception.MissingHazzardMethodMappingException;
import net.kyori.hazzard.exception.scan.UnscannableMethodException;
import net.kyori.hazzard.message.IMessageRenderer;
import net.kyori.hazzard.message.IMessageSender;
import net.kyori.hazzard.message.IMessageSource;
import net.kyori.hazzard.model.HazzardMethod;
import net.kyori.hazzard.placeholder.IPlaceholderResolver;
import net.kyori.hazzard.receiver.IReceiverLocatorResolver;
import net.kyori.hazzard.strategy.IPlaceholderResolverStrategy;
import net.kyori.hazzard.util.Weighted;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * The meta class for all of a Hazzard-driven proxy.
 *
 * @param <R> the receiver type
 * @param <I> the intermediate message type
 * @param <O> the output/rendered message type
 * @param <F> the finalised placeholder type, post-resolving
 */
@ThreadSafe
public final class Hazzard<R, I, O, F> {
  /**
   * The type which is being proxied with this Hazzard instance.
   */
  private final TypeToken<?> proxiedType;

  /**
   * The proxy invocation handler instance.
   */
  private final HazzardInvocationHandler<R, I, O, F> invocationHandler;

  /**
   * The strategy for resolving placeholders on a method invocation.
   */
  private final IPlaceholderResolverStrategy<R, I, F> placeholderResolverStrategy;

  /**
   * The source of intermediate messages, per receiver.
   */
  private final IMessageSource<R, I> messageSource;

  /**
   * The renderer of all messages, before sent via {@link #messageSender()}.
   */
  private final IMessageRenderer<R, I, O, F> messageRenderer;

  /**
   * The message sender of intermediate messages to a given receiver with resolved placeholders.
   */
  private final IMessageSender<R, O> messageSender;

  /**
   * A navigable set for iterating through the {@link IReceiverLocatorResolver}s with weight-based ordering.
   */
  private final NavigableSet<Weighted<? extends IReceiverLocatorResolver<? extends R>>> weightedReceiverLocatorResolvers;

  /**
   * A map of types to navigable sets for iterating through the {@link IPlaceholderResolver}s with weight-based
   * ordering.
   */
  private final Map<Type, NavigableSet<Weighted<? extends IPlaceholderResolver<? extends R, ?, ? extends F>>>> weightedPlaceholderResolvers;

  /**
   * All scanned methods of this proxy, excluding special-case methods such as {@code default} methods and any returning
   * {@link Hazzard}.
   */
  private final Map<Method, HazzardMethod<? extends R>> scannedMethods;

  Hazzard(final TypeToken<?> proxiedType,
      final IPlaceholderResolverStrategy<R, I, F> placeholderResolverStrategy,
      final IMessageSource<R, I> messageSource,
      final IMessageRenderer<R, I, O, F> messageRenderer,
      final IMessageSender<R, O> messageSender,
      final NavigableSet<Weighted<? extends IReceiverLocatorResolver<? extends R>>> weightedReceiverLocatorResolvers,
      final Map<Type, NavigableSet<Weighted<? extends IPlaceholderResolver<? extends R, ?, ? extends F>>>> weightedPlaceholderResolvers)
      throws UnscannableMethodException {
    this.proxiedType = proxiedType;
    this.placeholderResolverStrategy = placeholderResolverStrategy;
    this.messageSource = messageSource;
    this.messageRenderer = messageRenderer;
    this.messageSender = messageSender;
    this.weightedReceiverLocatorResolvers = Collections.unmodifiableNavigableSet(weightedReceiverLocatorResolvers);
    this.weightedPlaceholderResolvers = Collections.unmodifiableMap(weightedPlaceholderResolvers);

    final Method[] methods = GenericTypeReflector.erase(proxiedType.getType()).getMethods();
    final Map<Method, HazzardMethod<? extends R>> scannedMethods = new HashMap<>(methods.length);
    for (final Method method : methods) {
      if (method.isDefault() || method.getReturnType() == Hazzard.class) {
        continue;
      }

      final HazzardMethod<? extends R> hazzardMethod =
          new HazzardMethod<>(this, proxiedType, method);
      scannedMethods.put(method, hazzardMethod);
    }
    this.scannedMethods = Collections.unmodifiableMap(scannedMethods);

    this.invocationHandler = new HazzardInvocationHandler<>(this);
  }

  @SideEffectFree
  public static <T, R> HazzardBuilder.Receivers<T, R> builder(final TypeToken<T> proxiedType) {
    return HazzardBuilder.newBuilder(proxiedType);
  }

  /**
   * @return the type which is being proxied with this instance
   */
  @Pure
  public Type proxiedType() {
    return this.proxiedType.getType();
  }

  /**
   * @return the proxy invocation handler instance for the current {@link #proxiedType()}
   */
  @Pure
  public HazzardInvocationHandler<R, I, O, F> invocationHandler() {
    return this.invocationHandler;
  }

  /**
   * @return the current placeholder resolving strategy
   */
  @Pure
  public IPlaceholderResolverStrategy<R, I, F> placeholderResolverStrategy() {
    return this.placeholderResolverStrategy;
  }

  /**
   * @return an unmodifiable view of a navigable set for iterating through the available {@link
   * IReceiverLocatorResolver}s with weight-based ordering
   */
  @Pure
  public NavigableSet<Weighted<? extends IReceiverLocatorResolver<? extends R>>> weightedReceiverLocatorResolvers() {
    return this.weightedReceiverLocatorResolvers;
  }

  /**
   * @return an unmodifiable view of a map of types to navigable sets for iterating through the available {@link
   * IPlaceholderResolver}s with weight-based ordering
   */
  @Pure
  public Map<Type, NavigableSet<Weighted<? extends IPlaceholderResolver<? extends R, ?, ? extends F>>>> weightedPlaceholderResolvers() {
    return this.weightedPlaceholderResolvers;
  }

  /**
   * Find a scanned method by the given method mapping.
   *
   * @param method the method to find a scanned method for
   * @return the scanned method
   * @throws MissingHazzardMethodMappingException if a method mapping is missing somehow; this shouldn't happen, but
   * is here just in case
   */
  public HazzardMethod<? extends R> scannedMethod(final Method method) throws MissingHazzardMethodMappingException {
    final var scanned = this.scannedMethods.get(method);
    if (scanned == null) {
      throw new MissingHazzardMethodMappingException(this.proxiedType(), method);
    }

    return scanned;
  }

  /**
   * @return the source of intermediate messages, per receiver
   */
  public IMessageSource<R, I> messageSource() {
    return this.messageSource;
  }

  /**
   * @return the renderer of messages, used before sending via {@link #messageSender()}
   */
  public IMessageRenderer<R, I, O, F> messageRenderer() {
    return this.messageRenderer;
  }

  /**
   * @return the message sender of intermediate messages to a given receiver with resolved placeholders
   */
  public IMessageSender<R, O> messageSender() {
    return this.messageSender;
  }
}
