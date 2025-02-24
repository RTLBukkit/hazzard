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

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.kyori.hazzard.annotation.meta.ThreadSafe;
import net.kyori.hazzard.internal.ReflectiveUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The {@link InvocationHandler} for a {@link Hazzard}-driven {@link Proxy}.
 *
 * @param <ViewerT> the viewer type
 * @param <TemplateT> the template type
 * @param <ReplacementT> the replacement value type, post-resolving
 */
@ThreadSafe
/* package-private */ final class HazzardInvocationHandler<ViewerT, TemplateT, MessageT, ReplacementT> implements InvocationHandler {
  /**
   * An empty array to substitute a state of missing method arguments.
   */
  private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

  private final Hazzard<ViewerT, TemplateT, MessageT, ReplacementT> hazzard;

  HazzardInvocationHandler(final Hazzard<ViewerT, TemplateT, MessageT, ReplacementT> hazzard) {
    this.hazzard = hazzard;
  }

  @Override
  public @Nullable Object invoke(final Object proxy, final Method method, @Nullable Object @Nullable [] args)
      throws Throwable {
    // First we need to ensure this is not one of the _required_ implemented methods, as that would
    //   cause other exceptions later down the line and break expected behaviour of Java objects.
    if (isEqualsMethod(method)) {
      return this.proxiedEquals(args);
    } else if (isHashCodeMethod(method)) {
      return this.hazzard.hashCode();
    } else if (isToStringMethod(method)) {
      return this.proxiedToString();
    }

    // With that out of the way, get rid of nulls in our parameters.
    // We do not want a null array as that becomes inconvenient to us.
    if (args == null) {
      // As an empty array is immutable, there's also no reason not to just cache and reuse it.
      args = EMPTY_OBJECT_ARRAY;
    }

    // We have nothing to do if the user has specified a default implementation...
    if (method.isDefault()) {
      // ... in which case, find it and invoke it appropriately.
      final MethodHandle handle = ReflectiveUtils.findMethod(method, proxy);
      if (args.length == 0) {
        return handle.invoke();
      } else {
        return handle.invokeWithArguments(args);
      }
    }

    // If for some reason the user wants to access the Hazzard instance, we will let them do so,
    //   though it is not advised.
    if (method.getReturnType() == Hazzard.class) {
      return this.hazzard;
    }

    final var hazzardMethod = this.hazzard.scannedMethod(method);
    final ViewerT viewer = hazzardMethod.viewerLookupService().lookup(method, proxy, args);
    final TemplateT template = this.hazzard.templateLocator().templateOf(viewer, hazzardMethod.translationKey());
    final var resolvedPlaceholders =
        this.hazzard.templateVariableResolver()
            .resolveVariables(
                this.hazzard,
                viewer,
                template,
                hazzardMethod,
                args
            );
    final MessageT renderedMessage = this.hazzard.messageComposer().compose(
        viewer,
        template,
        resolvedPlaceholders,
        method,
        this.hazzard.proxiedType()
    );

    if (method.getReturnType() == void.class) {
      this.hazzard.messageSender().send(viewer, renderedMessage);
      return null;
    } else {
      return renderedMessage;
    }
  }

  private boolean proxiedEquals(final @Nullable Object @Nullable [] args) {
    if (args == null || args.length != 1) {
      return false;
    }

    return args[0] == this || args[0] == this.hazzard;
  }

  private String proxiedToString() {
    return GenericTypeReflector.getTypeName(this.hazzard.proxiedType())
        + '@' + this.hazzard.hashCode();
  }

  private static boolean isEqualsMethod(final Method method) {
    return "equals".equals(method.getName())
        && method.getParameterCount() == 1
        && method.getReturnType() == boolean.class;
  }

  private static boolean isHashCodeMethod(final Method method) {
    return "hashCode".equals(method.getName())
        && method.getParameterCount() == 0
        && method.getReturnType() == int.class;
  }

  private static boolean isToStringMethod(final Method method) {
    return "toString".equals(method.getName())
        && method.getParameterCount() == 0
        && method.getReturnType() == String.class;
  }
}
