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

import static net.kyori.hazzard.util.Unit.UNIT;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.leangen.geantyref.TypeToken;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EmptyTest {
  @Test
  void emptyTest() {
    assertThatCode(() ->
        Hazzard.builder(TypeToken.get(EmptyDefinition.class))
            .templateLocator((receiver, key) -> UNIT)
            .composed((receiver, intermediateMessage, resolvedPlaceholders, method, owner) -> UNIT)
            .sent((receiver, renderedMessage) -> {
            })
            .variableResolver((hazzard, receiver, intermediateText, hazzardMethod, parameters) -> Map.of())
            .create()
    ).doesNotThrowAnyException();
  }

  private interface EmptyDefinition {
  }
}
