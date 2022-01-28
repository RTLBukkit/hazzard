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
package net.kyori.hazzard.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import net.kyori.hazzard.annotation.meta.ThreadSafe;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;


@ThreadSafe
public final class Either<finalT, intermediateT> {
  private final @Nullable finalT finalResult;
  private final @Nullable intermediateT intermediateResult;

  @SuppressWarnings("UnnecessaryParentheses") // Checkstyle wants me to use invalid syntax :D
  private Either(final @Nullable finalT left, final @Nullable intermediateT right) {
    if ((left == null) == (right == null)) {
      throw new IllegalArgumentException("must be either left or right");
    }

    this.finalResult = left;
    this.intermediateResult = right;
  }

  @SideEffectFree
  public static <L, R> Either<L, R> finalResult(final L left) {
    return new Either<>(left, null);
  }

  @SideEffectFree
  public Optional<finalT> finalResult() {
    return Optional.ofNullable(this.leftRaw());
  }

  @SideEffectFree
  public static <L, R> Either<L, R> intermediateResult(final R right) {
    return new Either<>(null, right);
  }

  @SideEffectFree
  public Optional<intermediateT> intermediateResult() {
    return Optional.ofNullable(this.rightRaw());
  }

  @Pure
  public @Nullable finalT leftRaw() {
    return this.finalResult;
  }

  @Pure
  public @Nullable intermediateT rightRaw() {
    return this.intermediateResult;
  }

  @EnsuresNonNullIf(expression = "this.left", result = true)
  @Pure
  public boolean getFinalResult() {
    return this.finalResult != null;
  }

  @EnsuresNonNullIf(expression = "this.right", result = true)
  @Pure
  public boolean getIntermediateResult() {
    return this.intermediateResult != null;
  }

  @Pure
  public void ifLeft(final Consumer<finalT> leftConsumer) {
    if (this.leftRaw() != null) {
      leftConsumer.accept(this.leftRaw());
    }
  }

  @Pure
  public void ifRight(final Consumer<intermediateT> rightConsumer) {
    if (this.rightRaw() != null) {
      rightConsumer.accept(this.rightRaw());
    }
  }

  @Pure
  public void map(final Consumer<finalT> leftConsumer, final Consumer<intermediateT> rightConsumer) {
    if (this.leftRaw() != null) {
      leftConsumer.accept(this.leftRaw());
    } else if (this.rightRaw() != null) {
      rightConsumer.accept(this.rightRaw());
    } else {
      throw new IllegalStateException("either left or right must be non-null");
    }
  }

  @Pure
  @Override
  public boolean equals(final @Nullable Object other) {
    if (!(other instanceof final Either<?, ?> otherEither)) {
      return false;
    }

    return Objects.equals(otherEither.leftRaw(), this.leftRaw())
        && Objects.equals(otherEither.rightRaw(), this.rightRaw());
  }

  @Pure
  @Override
  public int hashCode() {
    if (this.getFinalResult()) {
      return this.leftRaw().hashCode();
    } else if (this.getIntermediateResult()) {
      return this.rightRaw().hashCode();
    } else {
      throw new IllegalStateException("either left or right must be non-null");
    }
  }

  @Override
  public String toString() {
    return "Either{" +
        "left=" + this.leftRaw() +
        ", right=" + this.rightRaw() +
        '}';
  }
}
