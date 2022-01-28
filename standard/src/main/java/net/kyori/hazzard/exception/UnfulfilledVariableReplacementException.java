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
package net.kyori.hazzard.exception;

import net.kyori.hazzard.internal.ReflectiveUtils;
import net.kyori.hazzard.model.HazzardMethod;

public final class UnfinishedPlaceholderException extends PlaceholderResolvingException {
  private final HazzardMethod<?> hazzardMethod;
  private final String placeholderName;
  private final Object placeholderValue;

  public UnfinishedPlaceholderException(final HazzardMethod<?> hazzardMethod, final String placeholderName,
      final Object placeholderValue) {
    super("The placeholder "
        + placeholderName
        + " was unfinished in method: "
        + ReflectiveUtils.formatMethodName(hazzardMethod.owner().getType(), hazzardMethod.reflectMethod()));
    this.hazzardMethod = hazzardMethod;
    this.placeholderName = placeholderName;
    this.placeholderValue = placeholderValue;
  }

  public HazzardMethod<?> hazzardMethod() {
    return this.hazzardMethod;
  }

  public String placeholderName() {
    return this.placeholderName;
  }

  public Object placeholderValue() {
    return this.placeholderValue;
  }
}
