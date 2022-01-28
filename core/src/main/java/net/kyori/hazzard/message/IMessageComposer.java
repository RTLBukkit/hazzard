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
package net.kyori.hazzard.message;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import net.kyori.hazzard.annotation.meta.ThreadSafe;

/**
 * A rendered of intermediate messages with resolved placeholders for a given receiver.
 *
 * @param <ViewerT> the eventual viewer type of this message
 * @param <MessageBuilderT> the intermediate message type
 * @param <MessageT> the output/rendered message type
 * @param <FinalizedPlaceholderT> the finalised placeholder type
 */
@FunctionalInterface
@ThreadSafe
public interface IMessageRenderer<ViewerT, MessageBuilderT, MessageT, FinalizedPlaceholderT> {
  /**
   * Render the intermediate message into a rendered message.
   * <p>
   * <b>Note:</b> This must be infallible, meaning any errors should be done earlier on.
   * </p>
   *
   * @param viewer the viewer of the message
   * @param messageBuilder the intermediate message to render
   * @param resolvedPlaceholders the resolved placeholders of this message
   * @param annotatedMethod the method invoked
   * @param owningType the type of the owning interface of the method
   * @return the rendered message
   */
  MessageT render(final ViewerT viewer, final MessageBuilderT messageBuilder, final Map<String, ? extends FinalizedPlaceholderT> resolvedPlaceholders,
                  final Method annotatedMethod, final Type owningType);
}
