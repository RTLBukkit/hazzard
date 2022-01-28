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
package net.kyori.hazzard.internal;

import java.util.Iterator;

public final class PrefixedDelegateIterator<PrefixT> implements Iterator<PrefixT> {
  private final PrefixT prefix;
  private final Iterator<PrefixT> delegate;
  private boolean seenPrefix = false;

  public PrefixedDelegateIterator(final PrefixT prefix, final Iterator<PrefixT> delegate) {
    this.prefix = prefix;
    this.delegate = delegate;
  }

  @Override
  public boolean hasNext() {
    return !this.seenPrefix || this.delegate.hasNext();
  }

  @Override
  public PrefixT next() {
    if (!this.seenPrefix) {
      this.seenPrefix = true;
      return this.prefix;
    }

    return this.delegate.next();
  }

  @Override
  public void remove() {
    if (!this.seenPrefix) {
      throw new IllegalStateException("must see prefix before removing from iterator");
    }

    this.delegate.remove();
  }
}
