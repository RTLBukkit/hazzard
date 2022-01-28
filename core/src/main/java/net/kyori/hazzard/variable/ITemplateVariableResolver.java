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
package net.kyori.hazzard.placeholder;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import net.kyori.hazzard.annotation.meta.ThreadSafe;
import net.kyori.hazzard.util.VariableWrapper;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A resolver for a placeholder of type {@link TemplateArgumentT}.
 *
 * @param <ViewerT> the viewer type
 * @param <TemplateArgumentT> the input placeholder type, or a supertype thereof
 * @param <TemplateReplacementT> the finalised placeholder type
 */
@FunctionalInterface
@ThreadSafe
public interface ITemplateVariableResolver<ViewerT, TemplateArgumentT, TemplateReplacementT> {
  static <ViewerT, T> ITemplateVariableResolver<ViewerT, T, T> identityPlaceholderResolver() {
    return (placeholderName, value, receiver, owner, method, parameters) ->
        Map.of(placeholderName, VariableWrapper.finalResult(ConclusionValue.conclusionValue(value)));
  }

  /**
   * Resolves a given variable into a result. In most cases, you want to return {@link
   * ContinuanceValue#continuanceValue(Object, Type) ContinuanceValue}s.
   *
   * @param variableName the name of the template argument that is currently being resolved; two results cannot share name,
   *     so this is only applicable as a prefix or for the map keys
   * @param value the value of the template argument, of type {@link TemplateArgumentT}
   * @param viewer the eventual viewer of the message
   * @param owner the owning interface type of the method
   * @param method the method called
   * @param parameters the parameters passed to the method
   * @return the resolved TemplateVariable replacement(s), or {@code null} if you wish to pass on the resolving to the
   * next resolver. The map must be {@code { placeholder name => state value }}
   */
  @Nullable Map<String, VariableWrapper<ConclusionValue<? extends TemplateReplacementT>, ContinuanceValue<?>>> resolve(
          final String variableName, final TemplateArgumentT value, final ViewerT viewer, final Type owner,
          final Method method, final @Nullable Object[] parameters);
}
