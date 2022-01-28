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
package net.kyori.hazzard.message;

import net.kyori.hazzard.annotation.meta.ThreadSafe;
import net.kyori.hazzard.exception.MissingTranslationKeyException;

/**
 * A service capable of retrieving intermediate localization templates for a given translation key.
 *
 * @param <ViewerT> the viewer type
 * @param <TemplateT> the intermediate message type
 */
@FunctionalInterface
@ThreadSafe
public interface TemplateLocator<ViewerT, TemplateT> {
  /**
   * Fetch the corresponding template for @code{translationKey} with the given {@code viewer}.
   *
   * @param viewer the eventual receiver of this message
   * @param translationKey the key of this message
   * @return the message found
   * @throws MissingTranslationKeyException if there is no message for this key found, and a thrown exception is
   * preferred over a generic message
   */
  TemplateT templateOf(final ViewerT viewer, final String translationKey) throws MissingTranslationKeyException;
}
