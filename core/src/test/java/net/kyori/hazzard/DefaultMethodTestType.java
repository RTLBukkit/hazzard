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

import net.kyori.hazzard.annotation.TranslationKey;
import net.kyori.hazzard.annotation.TemplateArgument;

public interface DefaultMethodTestType {
  String MESSAGE_KEY = "test";
  String DEFAULT_VALUE = "default placeholder value";

  @TranslationKey(MESSAGE_KEY)
  void method(@TemplateArgument final String placeholder);

  default void empty() {
    this.method(DEFAULT_VALUE);
  }

  default void withParameter(final String placeholder) {
    this.method(placeholder);
  }
}
