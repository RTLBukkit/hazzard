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

import java.util.Map;
import net.kyori.hazzard.Hazzard;
import net.kyori.hazzard.annotation.meta.ThreadSafe;
import net.kyori.hazzard.exception.VariableResolutionException;
import net.kyori.hazzard.model.HazzardMethod;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A strategy for resolving placeholders in a message.
 */
@FunctionalInterface
@ThreadSafe
public interface ITemplateVariableResolver<ViewerT, TemplateT, TemplateVariableValue> {
  /**
   * Resolve all template variables with the current strategy.
   *
   * @param hazzard the {@link Hazzard} meta instance for resolvers
   * @param template the invocation's template; can be useful to determine if any resolving should be done at all,
   *                 should variables be left unused in the template itself
   * @param hazzardMethod  the scanned method that was invoked
   * @param parameters       the parameters in the invocation to this method
   * @return a map of all resolved variables
   */
  Map<String, ? extends TemplateVariableValue> resolveVariables(final Hazzard<ViewerT, TemplateT, ?, TemplateVariableValue> hazzard, final ViewerT receiver,
                                                                final TemplateT template, final HazzardMethod<? extends ViewerT> hazzardMethod,
                                                                final @Nullable Object[] parameters)
      throws VariableResolutionException;
}
