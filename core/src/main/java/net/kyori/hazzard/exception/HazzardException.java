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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The general supertype to all exceptions Hazzard causes or might throw.
 */
public abstract class HazzardException extends Exception {
  protected HazzardException() {
  }

  protected HazzardException(final @Nullable String message) {
    super(message);
  }

  protected HazzardException(final @Nullable String message, final @Nullable Throwable cause) {
    super(message, cause);
  }

  protected HazzardException(final @Nullable Throwable cause) {
    super(cause);
  }

  protected HazzardException(final @Nullable String message, final @Nullable Throwable cause,
      final boolean enableSuppression, final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
