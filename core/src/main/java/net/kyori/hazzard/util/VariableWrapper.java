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
public final class VariableWrapper<finalT, intermediateT> {
  private final @Nullable finalT finalResult;
  private final @Nullable intermediateT intermediateResult;

  @SuppressWarnings("UnnecessaryParentheses") // Checkstyle wants me to use invalid syntax :D
  private VariableWrapper(final @Nullable finalT left, final @Nullable intermediateT right) {
    if ((left == null) == (right == null)) {
      throw new IllegalArgumentException("must be either left or right");
    }

    this.finalResult = left;
    this.intermediateResult = right;
  }

  @SideEffectFree
  public static <L, R> VariableWrapper<L, R> finalResult(final L left) {
    return new VariableWrapper<>(left, null);
  }

  @SideEffectFree
  public Optional<finalT> finalResult() {
    return Optional.ofNullable(this.finalRaw());
  }

  @SideEffectFree
  public static <L, R> VariableWrapper<L, R> intermediateResult(final R right) {
    return new VariableWrapper<>(null, right);
  }

  @SideEffectFree
  public Optional<intermediateT> intermediateResult() {
    return Optional.ofNullable(this.continuingRaw());
  }

  @Pure
  public @Nullable finalT finalRaw() {
    return this.finalResult;
  }

  @Pure
  public @Nullable intermediateT continuingRaw() {
    return this.intermediateResult;
  }

  @EnsuresNonNullIf(expression = "this.finalResult", result = true)
  @Pure
  public boolean getFinalResult() {
    return this.finalResult != null;
  }

  @EnsuresNonNullIf(expression = "this.intermediateResult", result = true)
  @Pure
  public boolean getIntermediateResult() {
    return this.intermediateResult != null;
  }

  @Pure
  public void ifFinal(final Consumer<finalT> finalConsumer) {
    if (this.finalRaw() != null) {
      finalConsumer.accept(this.finalRaw());
    }
  }

  @Pure
  public void ifContinuing(final Consumer<intermediateT> continuingConsumer) {
    if (this.continuingRaw() != null) {
      continuingConsumer.accept(this.continuingRaw());
    }
  }

  @Pure
  public void map(final Consumer<finalT> finalConsumer, final Consumer<intermediateT> continuingConsumer) {
    if (this.finalRaw() != null) {
      finalConsumer.accept(this.finalRaw());
    } else if (this.continuingRaw() != null) {
      continuingConsumer.accept(this.continuingRaw());
    } else {
      throw new IllegalStateException("either final or continuing must be non-null");
    }
  }

  @Pure
  @Override
  public boolean equals(final @Nullable Object other) {
    if (!(other instanceof final VariableWrapper<?, ?> otherVariableWrapper)) {
      return false;
    }

    return Objects.equals(otherVariableWrapper.finalRaw(), this.finalRaw())
        && Objects.equals(otherVariableWrapper.continuingRaw(), this.continuingRaw());
  }

  @Pure
  @Override
  public int hashCode() {
    if (this.getFinalResult()) {
      return this.finalRaw().hashCode();
    } else if (this.getIntermediateResult()) {
      return this.continuingRaw().hashCode();
    } else {
      throw new IllegalStateException("either final or continuing must be non-null");
    }
  }

  @Override
  public String toString() {
    return "Either{" +
        "final=" + this.finalRaw() +
        ", continuing=" + this.continuingRaw() +
        '}';
  }
}
