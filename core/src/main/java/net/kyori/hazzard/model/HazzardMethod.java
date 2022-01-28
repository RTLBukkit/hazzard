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
import net.kyori.hazzard.annotation.TranslationKey;
import net.kyori.hazzard.annotation.meta.ThreadSafe;
import net.kyori.hazzard.exception.scan.MissingTranslationKeyAnnotationException;
import net.kyori.hazzard.exception.scan.ViewerLookupNotFoundException;
import net.kyori.hazzard.exception.scan.UnscannableMethodException;
import net.kyori.hazzard.message.TemplateLocator;
import net.kyori.hazzard.viewer.IViewerLookupService;
import net.kyori.hazzard.viewer.IViewerLookupServiceLocator;
import net.kyori.hazzard.util.Weighted;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

/**
 * A data class for a scanned method.
 *
 * @param <ViewerT> the eventual receiver type of this message
 */
@ThreadSafe
public final class HazzardMethod<ViewerT> {
  /**
   * The owning/declaring type of this method.
   */
  private final TypeToken<?> owner;

  /**
   * The {@link Method reflected method} of this method.
   */
  private final Method reflectMethod;

  /**
   * The key for the message to pass to a {@link TemplateLocator message source}.
   */
  private final String translationKey;

  /**
   * The locator for a given receiver of this message.
   */
  private final IViewerLookupService<? extends ViewerT> viewerLookupService;

  public HazzardMethod(final Hazzard<ViewerT, ?, ?, ?> hazzard, final TypeToken<?> owner, final Method reflectMethod)
      throws UnscannableMethodException {
    this.owner = owner;
    this.reflectMethod = reflectMethod;

    final TranslationKey translationKey = this.findTranslationKeyAnnotation();
    this.translationKey = translationKey.value();

    this.viewerLookupService = this.findViewerLookupService(hazzard);
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
  public String translationKey() {
    return this.translationKey;
  }

  @Pure
  public IViewerLookupService<? extends ViewerT> viewerLookupService() {
    return this.viewerLookupService;
  }

  private TranslationKey findTranslationKeyAnnotation() throws MissingTranslationKeyAnnotationException {
    final @Nullable TranslationKey annotation = this.reflectMethod.getAnnotation(TranslationKey.class);
    //noinspection ConstantConditions -- this is completely not true. It may be null, per its Javadocs.
    if (annotation == null) {
      throw new MissingTranslationKeyAnnotationException(this.owner.getType(), this.reflectMethod);
    }

    return annotation;
  }

  private IViewerLookupService<? extends ViewerT> findViewerLookupService(final Hazzard<ViewerT, ?, ?, ?> hazzard)
      throws ViewerLookupNotFoundException {
    final Iterator<Weighted<? extends IViewerLookupServiceLocator<? extends ViewerT>>> locators =
        hazzard.viewerLookupServiceLocators().descendingIterator();

    while (locators.hasNext()) {
      final IViewerLookupServiceLocator<? extends ViewerT> serviceLocator = locators.next().value();
      final @Nullable IViewerLookupService<? extends ViewerT> lookupServiceResult =
              serviceLocator.resolve(this.reflectMethod, this.owner.getType());

      if (lookupServiceResult != null) {
        return lookupServiceResult;
      }
    }

    throw new ViewerLookupNotFoundException(this.owner.getType(), this.reflectMethod);
  }
}
