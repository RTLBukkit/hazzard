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
package net.kyori.hazzard.strategy;

import static java.util.Collections.emptyNavigableSet;

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.kyori.hazzard.Hazzard;
import net.kyori.hazzard.annotation.TemplateArgument;
import net.kyori.hazzard.annotation.meta.ThreadSafe;
import net.kyori.hazzard.exception.VariableResolutionException;
import net.kyori.hazzard.exception.UnfulfilledVariableReplacementException;
import net.kyori.hazzard.internal.PrefixedDelegateIterator;
import net.kyori.hazzard.model.HazzardMethod;
import net.kyori.hazzard.variable.IntermediateValue;
import net.kyori.hazzard.variable.ITemplateVariableResolver;
import net.kyori.hazzard.strategy.supertype.ISupertypeStrategy;
import org.checkerframework.checker.nullness.qual.Nullable;

@ThreadSafe
public final class StandardTemplateVariableResolution<ViewerT, TemplateT, ReplacementT> implements
        net.kyori.hazzard.strategy.ITemplateVariableResolver<ViewerT, TemplateT, ReplacementT> {
  private final ISupertypeStrategy supertypeStrategy;

  public StandardTemplateVariableResolution(final ISupertypeStrategy supertypeStrategy) {
    this.supertypeStrategy = supertypeStrategy;
  }

  @Override
  public Map<String, ? extends ReplacementT> resolveVariables(final Hazzard<ViewerT, TemplateT, ?, ReplacementT> hazzard,
                                                              final ViewerT receiver, final TemplateT template,
                                                              final HazzardMethod<? extends ViewerT> hazzardMethod,
                                                              final @Nullable Object[] parameters)
      throws VariableResolutionException {
    if (parameters.length == 0) {
      return Collections.emptyMap();
    }

    final Map<String, ReplacementT> finalisedPlaceholders = new LinkedHashMap<>(parameters.length);
    final Map<String, IntermediateValue<?>> resolvingPlaceholders = new LinkedHashMap<>(16);
    final Parameter[] methodParameters = hazzardMethod.reflectMethod().getParameters();
    final Type[] exactParameterTypes = GenericTypeReflector.getParameterTypes(
        hazzardMethod.reflectMethod(), hazzard.proxiedType());

    for (int idx = 0; idx < parameters.length; ++idx) {
      final Parameter parameter = methodParameters[idx];
      final @Nullable Object value = parameters[idx];
      if (value == null) {
        // Nothing to resolve with.
        continue;
      }

      final Type parameterType = GenericTypeReflector.getExactSubType(
          exactParameterTypes[idx], value.getClass());

      final @Nullable TemplateArgument templateArgument = parameter.getAnnotation(TemplateArgument.class);
      if (templateArgument == null) {
        // Nothing to resolve.
        continue;
      }

      final String placeholderName = templateArgument.value().isEmpty()
          ? parameter.getName()
          : templateArgument.value();
      resolvingPlaceholders
          .put(placeholderName, IntermediateValue.continuanceValue(value, parameterType));
    }

    this.resolvePlaceholder(hazzard, receiver, finalisedPlaceholders,
        resolvingPlaceholders, hazzardMethod, parameters);

    return finalisedPlaceholders;
  }

  /**
   * Resolve a single template argument.
   *
   * @param hazzard the hazzard instance
   * @param variableReplacements the finalised replacements
   * @param resolvingVariables the placeholders to resolve
   * @param hazzardMethod the method we are resolving a placeholder for
   */
  private void resolvePlaceholder(final Hazzard<ViewerT, TemplateT, ?, ReplacementT> hazzard, final ViewerT receiver,
                                  final Map<String, ReplacementT> variableReplacements,
                                  final Map<String, IntermediateValue<?>> resolvingVariables,
                                  final HazzardMethod<? extends ViewerT> hazzardMethod, final @Nullable Object[] parameters)
      throws UnfulfilledVariableReplacementException {
    final var weightedVariableResolver = hazzard.weightedVariableResolvers();

    // Shamelessly stealing ~~kashike's~~ mbaxter's joke
    dancing:
    while (!resolvingVariables.isEmpty()) {
      final var unresolvedVariables = resolvingVariables.entrySet().iterator();
      while (unresolvedVariables.hasNext()) {
        final var continuanceEntry = unresolvedVariables.next();
        final String continuanceVariableName = continuanceEntry.getKey();
        final Type type = continuanceEntry.getValue().type();
        final Object value = continuanceEntry.getValue().value();

        final Iterator<Type> hierarchyIterator =
            new PrefixedDelegateIterator<>(type, this.supertypeStrategy.hierarchyIterator(type));
        while (hierarchyIterator.hasNext()) {
          final Type supertype = hierarchyIterator.next();

          for (final var weighted : weightedVariableResolver.getOrDefault(supertype, emptyNavigableSet())) {
            @SuppressWarnings("unchecked") // This should be equivalent.
            final var variableResolver =
                (ITemplateVariableResolver<ViewerT, Object, ? extends ReplacementT>) weighted.value();

            final var result =
                variableResolver.resolve(continuanceVariableName, value, receiver,
                    hazzardMethod.owner().getType(),
                    hazzardMethod.reflectMethod(), parameters);
            if (result == null) {
              // The resolver did not want to resolve this; pass it on.
              continue;
            }

            unresolvedVariables.remove();

            result.forEach((resolvedName, resolvedValue) ->
                resolvedValue.map(conclusionValue -> variableReplacements
                        .put(resolvedName, conclusionValue.value()),
                    continuanceValue -> resolvingVariables.put(resolvedName, continuanceValue)));

            continue dancing;
          }
        }

        throw new UnfulfilledVariableReplacementException(hazzardMethod, continuanceVariableName, value);
      }
    }
  }
}
