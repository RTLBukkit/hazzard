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
import java.util.Map.Entry;
import java.util.function.Function;

/**
 * A standard formatter for strings using {@link String#replace(CharSequence, CharSequence)} with {@code
 * "${prefix}${value}${suffix}"} where {@code value} is converted using {@link #templateArgumentToStringConverter}.
 * <p>
 * Most people will want a more advanced formatter for Discord messages/embeds, Minecraft component messages, etc.
 */
public record StringMessageRenderer<ViewerT, TemplateT, MessageT, ReplacementT>(
    String prefix,
    String suffix,
    Function<TemplateT, String> intermediateToStringConverter,
    Function<String, MessageT> stringToOutputConverter,
    Function<ReplacementT, String> templateArgumentToStringConverter
) implements IMessageComposer<ViewerT, TemplateT, MessageT, ReplacementT> {
  @Override
  public MessageT compose(
      final ViewerT viewer,
      final TemplateT template,
      final Map<String, ? extends ReplacementT> replacementValues,
      final Method annotatedMethod,
      final Type owningType
  ) {
    var intermediate = this.intermediateToStringConverter.apply(template);
    for (final Entry<String, ? extends ReplacementT> entry : replacementValues.entrySet()) {
      intermediate = intermediate.replace(this.prefix + entry.getKey() + this.suffix,
          this.templateArgumentToStringConverter.apply(entry.getValue()));
    }
    return this.stringToOutputConverter.apply(intermediate);
  }
}
