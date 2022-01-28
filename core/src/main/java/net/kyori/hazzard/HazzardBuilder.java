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
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import net.kyori.hazzard.annotation.meta.NotThreadSafe;
import net.kyori.hazzard.exception.scan.UnscannableMethodException;
import net.kyori.hazzard.message.IMessageComposer;
import net.kyori.hazzard.message.IMessageSendingService;
import net.kyori.hazzard.message.TemplateLocator;
import net.kyori.hazzard.variable.ITemplateVariableResolver;
import net.kyori.hazzard.viewer.IViewerLookupServiceLocator;
import net.kyori.hazzard.util.Weighted;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.checkerframework.dataflow.qual.Deterministic;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

@NotThreadSafe // Technically wrong, but this is a builder.
public final class HazzardBuilder {
  private HazzardBuilder() {
  }

  @SideEffectFree
  static <T, R> Receivers<T, R> newBuilder(final TypeToken<T> proxiedType) {
    return new Receivers<>(proxiedType);
  }

  @NotThreadSafe
  public static final class Receivers<T, ViewerT> {
    private final NavigableSet<Weighted<? extends IViewerLookupServiceLocator<? extends ViewerT>>> weightedServiceLocators = new TreeSet<>();

    private final TypeToken<T> proxiedType;

    private Receivers(final TypeToken<T> proxiedType) {
      this.proxiedType = proxiedType;
    }

    @Pure
    public NavigableSet<Weighted<? extends IViewerLookupServiceLocator<? extends ViewerT>>> weightedReceiverLocatorResolvers() {
      return this.weightedServiceLocators;
    }

    @Deterministic
    public @This Receivers<T, ViewerT> viewerLookupServiceLocator(
        final Weighted<? extends IViewerLookupServiceLocator<? extends ViewerT>> viewerLookupServiceLocator) {
      this.weightedServiceLocators.add(viewerLookupServiceLocator);
      return this;
    }

    @Deterministic
    public @This Receivers<T, ViewerT> viewerLookupServiceLocator(
            final IViewerLookupServiceLocator<? extends ViewerT> viewerLookupServiceLocator, final int weight) {
      this.weightedServiceLocators.add(new Weighted<>(viewerLookupServiceLocator, weight));
      return this;
    }

    @SideEffectFree
    public <TemplateT> TemplateLocated<T, ViewerT, TemplateT> templateLocator(final TemplateLocator<ViewerT, TemplateT> templateLocator) {
      return new TemplateLocated<>(this.proxiedType, this.weightedServiceLocators, templateLocator);
    }
  }

  @NotThreadSafe // Technically wrong, but this is a builder.
  public static final class TemplateLocated<T, ViewerT, TemplateT> {
    private final TypeToken<T> proxiedType;
    private final NavigableSet<Weighted<? extends IViewerLookupServiceLocator<? extends ViewerT>>> weightedReceiverLocatorResolvers;
    private final TemplateLocator<ViewerT, TemplateT> templateLocator;

    private TemplateLocated(final TypeToken<T> proxiedType,
                            final NavigableSet<Weighted<? extends IViewerLookupServiceLocator<? extends ViewerT>>> viewerLookupServiceLocators,
                            final TemplateLocator<ViewerT, TemplateT> templateLocator) {
      this.proxiedType = proxiedType;
      this.weightedReceiverLocatorResolvers = viewerLookupServiceLocators;
      this.templateLocator = templateLocator;
    }

    @SideEffectFree
    public <MessageT, ReplacementT> Composed<T, ViewerT, TemplateT, MessageT, ReplacementT>
    composed(final IMessageComposer<ViewerT, TemplateT, MessageT, ReplacementT> messageComposer) {
      return new Composed<>(this.proxiedType, this.weightedReceiverLocatorResolvers, this.templateLocator, messageComposer);
    }
  }

  @NotThreadSafe // Technically wrong, but this is a builder.
  public static final class Composed<T, ViewerT, TemplateT, MessageT, ReplacementT> {
    private final TypeToken<T> proxiedType;
    private final NavigableSet<Weighted<? extends IViewerLookupServiceLocator<? extends ViewerT>>> lookupServiceLocators;
    private final TemplateLocator<ViewerT, TemplateT> templateLocator;
    private final IMessageComposer<ViewerT, TemplateT, MessageT, ReplacementT> messageComposer;

    private Composed(final TypeToken<T> proxiedType,
                     final NavigableSet<Weighted<? extends IViewerLookupServiceLocator<? extends ViewerT>>> weightedViewerLookupServiceLocators,
                     final TemplateLocator<ViewerT, TemplateT> templateLocator,
                     final IMessageComposer<ViewerT, TemplateT, MessageT, ReplacementT> messageComposer) {
      this.proxiedType = proxiedType;
      this.lookupServiceLocators = weightedViewerLookupServiceLocators;
      this.templateLocator = templateLocator;
      this.messageComposer = messageComposer;
    }

    @SideEffectFree
    public Sent<T, ViewerT, TemplateT, MessageT, ReplacementT> sent(final IMessageSendingService<ViewerT, MessageT> messageSender) {
      return new Sent<>(this.proxiedType, this.lookupServiceLocators, this.templateLocator,
          this.messageComposer, messageSender);
    }
  }

  @NotThreadSafe // Technically wrong, but this is a builder.
  public static final class Sent<T, ViewerT, TemplateT, MessageT, ReplacementT> {
    private final TypeToken<T> proxiedType;
    private final NavigableSet<Weighted<? extends IViewerLookupServiceLocator<? extends ViewerT>>> weightedViewerLookupServiceLocator;
    private final TemplateLocator<ViewerT, TemplateT> templateLocator;
    private final IMessageComposer<ViewerT, TemplateT, MessageT, ReplacementT> messageComposer;
    private final IMessageSendingService<ViewerT, MessageT> messageSender;

    private Sent(final TypeToken<T> proxiedType,
        final NavigableSet<Weighted<? extends IViewerLookupServiceLocator<? extends ViewerT>>> weightedViewerLookupServiceLocator,
        final TemplateLocator<ViewerT, TemplateT> templateLocator,
        final IMessageComposer<ViewerT, TemplateT, MessageT, ReplacementT> messageComposer,
        final IMessageSendingService<ViewerT, MessageT> messageSender) {
      this.proxiedType = proxiedType;
      this.weightedViewerLookupServiceLocator = weightedViewerLookupServiceLocator;
      this.templateLocator = templateLocator;
      this.messageComposer = messageComposer;
      this.messageSender = messageSender;
    }

    @SideEffectFree
    public Resolved<T, ViewerT, TemplateT, MessageT, ReplacementT> variableResolver(
        final net.kyori.hazzard.strategy.ITemplateVariableResolver<ViewerT, TemplateT, ReplacementT> variableResolver) {
      return new Resolved<>(this.proxiedType, this.weightedViewerLookupServiceLocator, this.templateLocator,
          this.messageComposer, this.messageSender, variableResolver);
    }
  }

  @NotThreadSafe
  public static final class Resolved<T, ViewerT, TemplateT, MessageT, ReplacementT> {
    private final TypeToken<T> proxiedType;
    private final NavigableSet<Weighted<? extends IViewerLookupServiceLocator<? extends ViewerT>>> weightedViewerLookupServiceLocator;
    private final TemplateLocator<ViewerT, TemplateT> templateLocator;
    private final IMessageComposer<ViewerT, TemplateT, MessageT, ReplacementT> messageComposer;
    private final IMessageSendingService<ViewerT, MessageT> messageSender;
    private final net.kyori.hazzard.strategy.ITemplateVariableResolver<ViewerT, TemplateT, ReplacementT> variableResolverStrategy;
    private final Map<Type, NavigableSet<Weighted<? extends ITemplateVariableResolver<? extends ViewerT, ?, ? extends ReplacementT>>>>
            weightedVariableResolvers = new HashMap<>();

    private Resolved(final TypeToken<T> proxiedType,
                     final NavigableSet<Weighted<? extends IViewerLookupServiceLocator<? extends ViewerT>>> weightedViewerLookupServiceLocator,
                     final TemplateLocator<ViewerT, TemplateT> templateLocator, final IMessageComposer<ViewerT, TemplateT, MessageT, ReplacementT> messageComposer,
                     final IMessageSendingService<ViewerT, MessageT> messageSender,
                     final net.kyori.hazzard.strategy.ITemplateVariableResolver<ViewerT, TemplateT, ReplacementT> variableResolverStrategy) {
      this.proxiedType = proxiedType;
      this.weightedViewerLookupServiceLocator = weightedViewerLookupServiceLocator;
      this.templateLocator = templateLocator;
      this.messageComposer = messageComposer;
      this.messageSender = messageSender;
      this.variableResolverStrategy = variableResolverStrategy;
    }

    @Deterministic
    public <ArgumentT> @This Resolved<T, ViewerT, TemplateT, MessageT, ReplacementT> weightedVariableResolver(
        final Class<? extends ArgumentT> resolvedType,
        final Weighted<? extends ITemplateVariableResolver<? extends ViewerT, ? super ArgumentT, ? extends ReplacementT>> weightedVariableResolver) {
      this.weightedVariableResolvers.computeIfAbsent(resolvedType, ignored -> new TreeSet<>())
          .add(weightedVariableResolver);
      return this;
    }

    @Deterministic
    public <Z> @This Resolved<T, ViewerT, TemplateT, MessageT, ReplacementT> weightedVariableResolver(
        final TypeToken<? extends Z> resolvedType,
        final Weighted<? extends ITemplateVariableResolver<? extends ViewerT, ? super Z, ? extends ReplacementT>> weightedVariableResolver) {
      this.weightedVariableResolvers.computeIfAbsent(resolvedType.getType(), ignored -> new TreeSet<>())
          .add(weightedVariableResolver);
      return this;
    }

    @Deterministic
    public <ArgumentT> @This Resolved<T, ViewerT, TemplateT, MessageT, ReplacementT> weightedVariableResolver(
        final Class<? extends ArgumentT> resolvedType,
        final ITemplateVariableResolver<? extends ViewerT, ? super ArgumentT, ? extends ReplacementT> templateVariableResolver,
        final int weight) {
      this.weightedVariableResolvers.computeIfAbsent(resolvedType, ignored -> new TreeSet<>())
          .add(new Weighted<>(templateVariableResolver, weight));
      return this;
    }

    @Deterministic
    public <ArgumentT> @This Resolved<T, ViewerT, TemplateT, MessageT, ReplacementT> weightedVariableResolver(
        final TypeToken<? extends ArgumentT> resolvedType,
        final ITemplateVariableResolver<? extends ViewerT, ? super ArgumentT, ? extends ReplacementT> placeholderResolver,
        final int weight) {
      this.weightedVariableResolvers.computeIfAbsent(resolvedType.getType(), ignored -> new TreeSet<>())
          .add(new Weighted<>(placeholderResolver, weight));
      return this;
    }

    @SideEffectFree
    public T create() throws UnscannableMethodException {
      return this.create(Thread.currentThread().getContextClassLoader());
    }

    @SuppressWarnings("unchecked") // Proxy returns Object; we expect T which is provided in #proxiedType.
    @SideEffectFree
    public T create(final ClassLoader classLoader) throws UnscannableMethodException {
      final Hazzard<ViewerT, TemplateT, MessageT, ReplacementT> hazzard = new Hazzard<>(this.proxiedType, this.variableResolverStrategy,
          this.templateLocator, this.messageComposer, this.messageSender, this.weightedViewerLookupServiceLocator,
          this.weightedVariableResolvers);
      return (T) Proxy.newProxyInstance(classLoader,
          new Class[]{GenericTypeReflector.erase(this.proxiedType.getType())},
          hazzard.invocationHandler());
    }
  }
}
