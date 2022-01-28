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
import net.kyori.hazzard.message.IMessageComposer;
import net.kyori.hazzard.message.IMessageSendingService;
import net.kyori.hazzard.message.TemplateLocator;
import net.kyori.hazzard.model.HazzardMethod;
import net.kyori.hazzard.variable.ITemplateVariableResolver;
import net.kyori.hazzard.viewer.IViewerLookupServiceLocator;
import net.kyori.hazzard.util.Weighted;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * The meta class for all of a Hazzard-driven proxy.
 *
 * @param <ViewerT> the message receiving type
 * @param <TemplateT> the intermediate message type e.g. a localization string with placeholders.
 * @param <MessageT> the output/rendered message type e.g. a richly formatted string.
 * @param <VariableReplacementT> the finalised placeholder type, post-resolving, usually a component piece of MessageT
 */
@ThreadSafe
public final class Hazzard<ViewerT, TemplateT, MessageT, VariableReplacementT> {
  /**
   * The type which is being proxied with this Hazzard instance.
   */
  private final TypeToken<?> proxiedType;

  /**
   * The proxy invocation handler instance.
   */
  private final HazzardInvocationHandler<ViewerT, TemplateT, MessageT, VariableReplacementT> invocationHandler;

  /**
   * The strategy for resolving template variables upon method invocation.
   */
  private final net.kyori.hazzard.strategy.ITemplateVariableResolver<ViewerT, TemplateT, VariableReplacementT> templateVariableResolver;

  /**
   * The source of templates, potentially discriminated by viewer type.
   */
  private final TemplateLocator<ViewerT, TemplateT> templateLocator;

  /**
   * The template populator of all messages, before being sent via {@link #messageSender()}.
   */
  private final IMessageComposer<ViewerT, TemplateT, MessageT, VariableReplacementT> messageComposer;

  /**
   * The message sender of messages to a given viewer with resolved variable replacements.
   */
  private final IMessageSendingService<ViewerT, MessageT> messageSender;

  /**
   * A navigable set for iterating through the {@link IViewerLookupServiceLocator}s with weight-based ordering.
   */
  private final NavigableSet<Weighted<? extends IViewerLookupServiceLocator<? extends ViewerT>>> weightedViewerLookupResolvers;

  /**
   * A map of types to navigable sets for iterating through the {@link ITemplateVariableResolver}s with weight-based
   * ordering.
   */
  private final Map<Type, NavigableSet<Weighted<? extends ITemplateVariableResolver<? extends ViewerT, ?, ? extends VariableReplacementT>>>> weightedTemplateVariableResolver;

  /**
   * All scanned methods of this proxy, excluding special-case methods such as {@code default} methods and any returning
   * {@link Hazzard}.
   */
  private final Map<Method, HazzardMethod<? extends ViewerT>> scannedMethods;

  Hazzard(final TypeToken<?> proxiedType,
      final net.kyori.hazzard.strategy.ITemplateVariableResolver<ViewerT, TemplateT, VariableReplacementT> templateVariableResolver,
      final TemplateLocator<ViewerT, TemplateT> templateLocator,
      final IMessageComposer<ViewerT, TemplateT, MessageT, VariableReplacementT> messageComposer,
      final IMessageSendingService<ViewerT, MessageT> messageSender,
      final NavigableSet<Weighted<? extends IViewerLookupServiceLocator<? extends ViewerT>>> weightedViewerLookupResolvers,
      final Map<Type, NavigableSet<Weighted<? extends ITemplateVariableResolver<? extends ViewerT, ?, ? extends VariableReplacementT>>>> weightedTemplateVariableResolver)
      throws UnscannableMethodException {
    this.proxiedType = proxiedType;
    this.templateVariableResolver = templateVariableResolver;
    this.templateLocator = templateLocator;
    this.messageComposer = messageComposer;
    this.messageSender = messageSender;
    this.weightedViewerLookupResolvers = Collections.unmodifiableNavigableSet(weightedViewerLookupResolvers);
    this.weightedTemplateVariableResolver = Collections.unmodifiableMap(weightedTemplateVariableResolver);

    final Method[] methods = GenericTypeReflector.erase(proxiedType.getType()).getMethods();
    final Map<Method, HazzardMethod<? extends ViewerT>> scannedMethods = new HashMap<>(methods.length);
    for (final Method method : methods) {
      if (method.isDefault() || method.getReturnType() == Hazzard.class) {
        continue;
      }

      final HazzardMethod<? extends ViewerT> hazzardMethod =
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
  public HazzardInvocationHandler<ViewerT, TemplateT, MessageT, VariableReplacementT> invocationHandler() {
    return this.invocationHandler;
  }

  /**
   * @return the current template variable resolving strategy
   */
  @Pure
  public net.kyori.hazzard.strategy.ITemplateVariableResolver<ViewerT, TemplateT, VariableReplacementT> templateVariableResolver() {
    return this.templateVariableResolver;
  }

  /**
   * @return an unmodifiable view of a navigable set for iterating through the available {@link
   * IViewerLookupServiceLocator}s with weight-based ordering
   */
  @Pure
  public NavigableSet<Weighted<? extends IViewerLookupServiceLocator<? extends ViewerT>>> viewerLookupServiceLocators() {
    return this.weightedViewerLookupResolvers;
  }

  /**
   * @return an unmodifiable view of a map of types to navigable sets for iterating through the available {@link
   * ITemplateVariableResolver}s with weight-based ordering
   */
  @Pure
  public Map<Type, NavigableSet<Weighted<? extends ITemplateVariableResolver<? extends ViewerT, ?, ? extends VariableReplacementT>>>> weightedVariableResolvers() {
    return this.weightedTemplateVariableResolver;
  }

  /**
   * Find a scanned method by the given method mapping.
   *
   * @param method the method to find a scanned method for
   * @return the scanned method
   * @throws MissingHazzardMethodMappingException if a method mapping is missing somehow; this shouldn't happen, but
   * is here just in case
   */
  public HazzardMethod<? extends ViewerT> scannedMethod(final Method method) throws MissingHazzardMethodMappingException {
    final var scanned = this.scannedMethods.get(method);
    if (scanned == null) {
      throw new MissingHazzardMethodMappingException(this.proxiedType(), method);
    }

    return scanned;
  }

  /**
   * @return the source of Templates, per ViewerT
   */
  public TemplateLocator<ViewerT, TemplateT> templateLocator() {
    return this.templateLocator;
  }

  /**
   * @return the composer of messages, used before sending via {@link #messageSender()}
   */
  public IMessageComposer<ViewerT, TemplateT, MessageT, VariableReplacementT> messageComposer() {
    return this.messageComposer;
  }

  /**
   * @return the message sender of intermediate messages to a given receiver with resolved placeholders
   */
  public IMessageSendingService<ViewerT, MessageT> messageSender() {
    return this.messageSender;
  }
}
