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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.leangen.geantyref.TypeToken;
import java.util.Map;
import net.kyori.hazzard.message.IMessageComposer;
import net.kyori.hazzard.message.IMessageSendingService;
import net.kyori.hazzard.message.TemplateLocator;
import net.kyori.hazzard.strategy.StandardTemplateVariableResolution;
import net.kyori.hazzard.strategy.supertype.StandardSupertypeThenInterfaceSupertypeStrategy;
import net.kyori.hazzard.util.Unit;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
class DefaultMethodTest {

  @Test
  void emptyDefaultMethodTest() throws Exception {
    final TemplateLocator<Unit, Unit> source = mock(TemplateLocator.class);
    final IMessageComposer<Unit, Unit, Unit, Unit> renderer = mock(IMessageComposer.class);
    final IMessageSendingService<Unit, Unit> sender = mock(IMessageSendingService.class);
    when(source.templateOf(any(), any())).thenReturn(UNIT);
    when(renderer.compose(any(), any(), any(), any(), any())).thenReturn(UNIT);

    assertThatCode(() ->
        Hazzard.<DefaultMethodTestType, Unit>builder(TypeToken.get(DefaultMethodTestType.class))
            .viewerLookupServiceLocator((method, proxy) -> (method1, proxy1, parameters) -> UNIT, 1)
            .templateLocator(source)
            .composed(renderer)
            .sent(sender)
            .variableResolver(new StandardTemplateVariableResolution<>(
                new StandardSupertypeThenInterfaceSupertypeStrategy(false)
            ))
            .weightedVariableResolver(String.class,
                (placeholderName, value, receiver, owner, method, parameters) -> Map.of(), 1)
            .create()
            .empty()
    ).doesNotThrowAnyException();

    verify(source).templateOf(UNIT, DefaultMethodTestType.MESSAGE_KEY);
    verify(sender).send(UNIT, UNIT);
  }

  @Test
  void defaultMethodTest() throws Exception {
    final TemplateLocator<Unit, Unit> source = mock(TemplateLocator.class);
    final IMessageComposer<Unit, Unit, Unit, Unit> renderer = mock(IMessageComposer.class);
    final IMessageSendingService<Unit, Unit> sender = mock(IMessageSendingService.class);
    when(source.templateOf(any(), any())).thenReturn(UNIT);
    when(renderer.compose(any(), any(), any(), any(), any())).thenReturn(UNIT);

    assertThatCode(() ->
        Hazzard.<DefaultMethodTestType, Unit>builder(TypeToken.get(DefaultMethodTestType.class))
            .viewerLookupServiceLocator((method, proxy) -> (method1, proxy1, parameters) -> UNIT, 1)
            .templateLocator(source)
            .composed(renderer)
            .sent(sender)
            .variableResolver(new StandardTemplateVariableResolution<>(
                new StandardSupertypeThenInterfaceSupertypeStrategy(false)
            ))
            .weightedVariableResolver(String.class,
                (placeholderName, value, receiver, owner, method, parameters) -> Map.of(), 1)
            .create()
            .withParameter(DefaultMethodTestType.DEFAULT_VALUE)
    ).doesNotThrowAnyException();

    verify(source).templateOf(UNIT, DefaultMethodTestType.MESSAGE_KEY);
    verify(sender).send(UNIT, UNIT);
  }

}
