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

import static java.util.Collections.emptyMap;
import static net.kyori.hazzard.util.Unit.UNIT;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import net.kyori.hazzard.annotation.TranslationKey;
import net.kyori.hazzard.annotation.TemplateArgument;
import net.kyori.hazzard.message.IMessageComposer;
import net.kyori.hazzard.message.IMessageSendingService;
import net.kyori.hazzard.message.TemplateLocator;
import net.kyori.hazzard.model.HazzardMethod;
import net.kyori.hazzard.variable.ReplacementResult;
import net.kyori.hazzard.variable.IntermediateValue;
import net.kyori.hazzard.strategy.ITemplateVariableResolver;
import net.kyori.hazzard.strategy.StandardTemplateVariableResolution;
import net.kyori.hazzard.strategy.supertype.StandardSupertypeThenInterfaceSupertypeStrategy;
import net.kyori.hazzard.util.VariableWrapper;
import net.kyori.hazzard.util.Unit;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

/* package-private */ class SimpleHazzardTest {
  @Test
  void emptyHazzardInstance() {
    assertThatCode(() ->
        Hazzard.<EmptyHazzardType, String>builder(TypeToken.get(EmptyHazzardType.class))
            .viewerLookupServiceLocator((method, proxy) -> (method1, proxy1, parameters) -> "receiver",
                2)
            .templateLocator((receiver, messageKey) -> UNIT)
            .composed(
                (receiver, intermediateMessage, resolvedPlaceholders, method, owner) -> UNIT)
            .sent((receiver, renderedMessage) -> {
            })
            .variableResolver(new EmptyResolving<>())
            .create()
    ).doesNotThrowAnyException();
  }

  @SuppressWarnings("unchecked")
  @Test
  void singleEmptyMethod() throws Exception {
    final TemplateLocator<Unit, Unit> messageSource = mock(TemplateLocator.class);
    final IMessageComposer<Unit, Unit, Unit, Unit> messageRenderer = mock(IMessageComposer.class);
    final IMessageSendingService<Unit, Unit> messageSender = mock(IMessageSendingService.class);
    when(messageSource.templateOf(any(), any())).thenReturn(UNIT);
    when(messageRenderer.compose(any(), any(), any(), any(), any())).thenReturn(UNIT);

    assertThatCode(() ->
        Hazzard.<SingleEmptyMethodHazzardType, Unit>builder(
                TypeToken.get(SingleEmptyMethodHazzardType.class))
            .viewerLookupServiceLocator((method, proxy) -> (method1, proxy1, parameters) -> UNIT,
                2)
            .templateLocator(messageSource)
            .composed(messageRenderer)
            .sent(messageSender)
            .variableResolver(new EmptyResolving<>())
            .create()
            .method()
    ).doesNotThrowAnyException();
  }

  @SuppressWarnings("unchecked")
  @Test
  void singleMethodStringPlaceholders() throws Exception {
    final TemplateLocator<TestableReceiver, String> messageSource = mock(TemplateLocator.class);
    final IMessageComposer<TestableReceiver, String, String, String> messageRenderer = spy(
        new SimpleStringFormatRenderer<>());
    final IMessageSendingService<TestableReceiver, String> messageSender = mock(IMessageSendingService.class);
    final TestableReceiver receiver = mock(TestableReceiver.class);
    when(messageSource.templateOf(any(), any())).thenReturn("Hello, %2$s!");

    assertThatCode(() ->
        Hazzard.<SingleMethodStringPlaceholdersHazzardType, TestableReceiver>builder(
                TypeToken.get(SingleMethodStringPlaceholdersHazzardType.class))
            .viewerLookupServiceLocator((method, proxy) -> (method1, proxy1, parameters) -> receiver,
                -1)
            .templateLocator(messageSource)
            .composed(messageRenderer)
            .sent(messageSender)
            .variableResolver(new StandardTemplateVariableResolution<>(
                new StandardSupertypeThenInterfaceSupertypeStrategy(false)))
            .weightedVariableResolver(String.class,
                (placeholderName, value, receiver1, owner, method, parameters) -> null,
                3)
            .weightedVariableResolver(String.class,
                (placeholderName, value, receiver1, owner, method, parameters) ->
                    Map.of(placeholderName, VariableWrapper.finalResult(ReplacementResult.conclusionValue(value))),
                1)
            .weightedVariableResolver(TypeToken.get(StringPlaceholderValue.class),
                (placeholderName, value, receiver1, owner, method, parameters) ->
                    Map.of(placeholderName, VariableWrapper.intermediateResult(
                        IntermediateValue.continuanceValue(value.value(), String.class))),
                1)
            .create()
            .method(receiver, "first", new SimpleStringPlaceholder("second"))
    ).doesNotThrowAnyException();

    verify(messageSource).templateOf(receiver, "test");
    verify(messageRenderer).compose(receiver, "Hello, %2$s!",
        new LinkedHashMap<>(Map.of("placeholder", "first", "cringe", "second")),
        SingleMethodStringPlaceholdersHazzardType.class.getMethods()[0],
        TypeToken.get(SingleMethodStringPlaceholdersHazzardType.class).getType());
    verify(messageSender).send(receiver, "Hello, second!");
  }

  @Test
  void defaultMethodOneParam() throws Exception {
    final TemplateLocator<TestableReceiver, String> messageSource = mock(TemplateLocator.class);
    final IMessageComposer<TestableReceiver, String, String, String> messageRenderer = spy(
        new SimpleStringFormatRenderer<>());
    final IMessageSendingService<TestableReceiver, String> messageSender = mock(IMessageSendingService.class);
    final TestableReceiver receiver = mock(TestableReceiver.class);
    when(messageSource.templateOf(any(), any())).thenReturn("Hello, %1$s!");

    assertThatCode(() ->
        Hazzard.<DefaultMethodNoParams, TestableReceiver>builder(TypeToken.get(DefaultMethodNoParams.class))
            .viewerLookupServiceLocator((method, proxy) -> (method1, proxy1, parameters) -> receiver, -1)
            .templateLocator(messageSource)
            .composed(messageRenderer)
            .sent(messageSender)
            .variableResolver(new StandardTemplateVariableResolution<>(
                new StandardSupertypeThenInterfaceSupertypeStrategy(false)
            ))
            .weightedVariableResolver(String.class,
                (placeholderName, value, receiver1, owner, method, parameters) ->
                    Map.of(placeholderName, VariableWrapper.finalResult(ReplacementResult.conclusionValue(value))), 1)
            .create()
            .method(receiver)
    ).doesNotThrowAnyException();

    verify(messageSource).templateOf(receiver, "test");
    verify(messageRenderer).compose(receiver, "Hello, %1$s!",
        new LinkedHashMap<>(Map.of("placeholder", "placeholder value")),
        DefaultMethodNoParams.class.getMethods()[0],
        TypeToken.get(DefaultMethodNoParams.class).getType());
    verify(messageSender).send(receiver, "Hello, placeholder value!");
  }

  interface EmptyHazzardType {
  }

  interface SingleEmptyMethodHazzardType {
    @TranslationKey("test")
    void method();
  }

  interface SingleMethodStringPlaceholdersHazzardType {
    @TranslationKey("test")
    void method(
        final TestableReceiver receiver,
        @TemplateArgument final String placeholder,
        @TemplateArgument("cringe") final SimpleStringPlaceholder placeholder2
    );
  }

  interface DefaultMethodNoParams {
    @TranslationKey("test")
    void method(
        final TestableReceiver receiver,
        @TemplateArgument final String placeholder
    );

    default void method(final TestableReceiver receiver) {
      this.method(receiver, "placeholder value");
    }
  }

  private static class TestableReceiver {
    void send(final Object message) {
      fail("TestableReceiver#send must be mocked");
    }
  }

  private static class EmptyResolving<R, I, F> implements
          ITemplateVariableResolver<R, I, F> {
    @Override
    public Map<String, ? extends F> resolveVariables(final Hazzard<R, I, ?, F> hazzard,
                                                     final R receiver, final I template,
                                                     final HazzardMethod<? extends R> hazzardMethod,
                                                     final @Nullable Object[] parameters) {
      return emptyMap();
    }
  }

  private static class SimpleStringFormatRenderer<R> implements
          IMessageComposer<R, String, String, String> {
    @Override
    public String compose(final R viewer, final String template,
                          final Map<String, ? extends String> replacementValues, final Method annotatedMethod,
                          final Type owningType) {
      return String.format(template, replacementValues.values().toArray());
    }
  }

  private interface StringPlaceholderValue {
    String value();
  }

  private static class SimpleStringPlaceholder implements StringPlaceholderValue {
    private final String value;

    private SimpleStringPlaceholder(final String value) {
      this.value = value;
    }

    @Override
    public String value() {
      return this.value;
    }
  }
}
