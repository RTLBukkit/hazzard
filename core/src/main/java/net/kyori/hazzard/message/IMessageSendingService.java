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

import net.kyori.hazzard.annotation.meta.ThreadSafe;

/**
 * A trait that defines how to send finalised messages to the given viewer.
 *
 * @param <ViewerT> the viewer type of the message
 * @param <OutputT> the output/rendered message
 */
@FunctionalInterface
@ThreadSafe
public interface IMessageSendingService<ViewerT, OutputT> {
  /**
   * Send the message to the given receiver.
   *
   * @param viewer the receiver of the message
   * @param renderedMessage the rendered message to send to the receiver
   */
  void send(ViewerT viewer, OutputT renderedMessage);
}
