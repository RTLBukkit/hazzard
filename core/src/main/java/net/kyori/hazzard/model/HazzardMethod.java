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
package net.kyori.hazzard.model;

import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Method;
import java.util.Iterator;
import net.kyori.hazzard.Hazzard;
import net.kyori.hazzard.annotation.Message;
import net.kyori.hazzard.annotation.meta.ThreadSafe;
import net.kyori.hazzard.exception.scan.MissingMessageAnnotationException;
import net.kyori.hazzard.exception.scan.NoReceiverLocatorFoundException;
import net.kyori.hazzard.exception.scan.UnscannableMethodException;
import net.kyori.hazzard.message.IMessageSource;
import net.kyori.hazzard.receiver.IReceiverLocator;
import net.kyori.hazzard.receiver.IReceiverLocatorResolver;
import net.kyori.hazzard.util.Weighted;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

/**
 * A data class for a scanned method.
 *
 * @param <R> the eventual receiver type of this message
 */
@ThreadSafe
public final class HazzardMethod<R> {
  /**
   * The owning/declaring type of this method.
   */
  private final TypeToken<?> owner;

  /**
   * The {@link Method reflected method} of this method.
   */
  private final Method reflectMethod;

  /**
   * The key for the message to pass to a {@link IMessageSource message source}.
   */
  private final String messageKey;

  /**
   * The locator for a given receiver of this message.
   */
  private final IReceiverLocator<? extends R> receiverLocator;

  public HazzardMethod(final Hazzard<R, ?, ?, ?> hazzard, final TypeToken<?> owner, final Method reflectMethod)
      throws UnscannableMethodException {
    this.owner = owner;
    this.reflectMethod = reflectMethod;

    final Message message = this.findMessageAnnotation();
    this.messageKey = message.value();

    this.receiverLocator = this.findReceiverLocator(hazzard);
  }

  @Pure
  public TypeToken<?> owner() {
    return this.owner;
  }

  @Pure
  public Method reflectMethod() {
    return this.reflectMethod;
  }

  @Pure
  public String messageKey() {
    return this.messageKey;
  }

  @Pure
  public IReceiverLocator<? extends R> receiverLocator() {
    return this.receiverLocator;
  }

  private Message findMessageAnnotation() throws MissingMessageAnnotationException {
    final @Nullable Message annotation = this.reflectMethod.getAnnotation(Message.class);
    //noinspection ConstantConditions -- this is completely not true. It may be null, per its Javadocs.
    if (annotation == null) {
      throw new MissingMessageAnnotationException(this.owner.getType(), this.reflectMethod);
    }

    return annotation;
  }

  private IReceiverLocator<? extends R> findReceiverLocator(final Hazzard<R, ?, ?, ?> hazzard)
      throws NoReceiverLocatorFoundException {
    final Iterator<Weighted<? extends IReceiverLocatorResolver<? extends R>>> receiverLocatorResolverIterator =
        hazzard.weightedReceiverLocatorResolvers().descendingIterator();

    while (receiverLocatorResolverIterator.hasNext()) {
      final IReceiverLocatorResolver<? extends R> receiverLocatorResolver =
          receiverLocatorResolverIterator.next().value();
      final @Nullable IReceiverLocator<? extends R> resolvedLocator =
          receiverLocatorResolver.resolve(this.reflectMethod, this.owner.getType());

      if (resolvedLocator != null) {
        return resolvedLocator;
      }
    }

    throw new NoReceiverLocatorFoundException(this.owner.getType(), this.reflectMethod);
  }
}
