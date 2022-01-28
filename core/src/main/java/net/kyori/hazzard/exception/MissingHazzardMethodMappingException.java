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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import net.kyori.hazzard.internal.ReflectiveUtils;

public final class MissingHazzardMethodMappingException extends HazzardException {
  private final Type owner;
  private final Method method;

  public MissingHazzardMethodMappingException(final Type owner, final Method method) {
    super("A method was not mapped by Hazzard: " + ReflectiveUtils.formatMethodName(owner, method));
    this.owner = owner;
    this.method = method;
  }

  public Type owner() {
    return this.owner;
  }

  public Method method() {
    return this.method;
  }
}
