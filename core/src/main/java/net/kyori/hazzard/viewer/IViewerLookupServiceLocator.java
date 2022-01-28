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
package net.kyori.hazzard.receiver;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import net.kyori.hazzard.Resolver;
import net.kyori.hazzard.annotation.meta.ThreadSafe;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A resolver to find a {@link IViewerLookupService} to use for a given method.
 *
 * @param <ViewerT> the output receiver type
 */
@FunctionalInterface
@ThreadSafe
public interface IViewerLookupServiceLocator<ViewerT> {
  /**
   * Resolves a {@link IViewerLookupService} to use for the given method going forwards.
   *
   * @param method the method to resolve a {@link IViewerLookupService} for
   * @param proxy  the proxied type the {@code method} belongs to
   * @return the found {@link IViewerLookupService}, or {@code null} to fail resolving of class with an exception
   */
  @Nullable IViewerLookupService<ViewerT> resolve(final Method method, final Type proxy);
}
