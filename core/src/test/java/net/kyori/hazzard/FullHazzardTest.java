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

import static net.kyori.hazzard.variable.ReplacementResult.conclusionValue;
import static net.kyori.hazzard.variable.IntermediateValue.continuanceValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.leangen.geantyref.TypeToken;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.kyori.examination.Examinable;
import net.kyori.examination.ExaminableProperty;
import net.kyori.examination.string.StringExaminer;
import net.kyori.hazzard.annotation.TemplateArgument;
import net.kyori.hazzard.annotation.TranslationKey;
import net.kyori.hazzard.variable.ReplacementResult;
import net.kyori.hazzard.variable.IntermediateValue;
import net.kyori.hazzard.variable.ITemplateVariableResolver;
import net.kyori.hazzard.viewer.IViewerLookupService;
import net.kyori.hazzard.viewer.IViewerLookupServiceLocator;
import net.kyori.hazzard.strategy.StandardTemplateVariableResolution;
import net.kyori.hazzard.strategy.supertype.StandardSupertypeThenInterfaceSupertypeStrategy;
import net.kyori.hazzard.util.VariableWrapper;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

class FullHazzardTest {
  @Test
  void fullyFledgedHazzardInstance() throws Exception {
    final MockedReceiver receiver = mock(MockedReceiver.class);
    final Email email = new Email("Me <me@me.me>", "Your car's extended warranty",
        "Hi,\n\nI wish to speak to you about your car's extended warranty.");

    final HazzardType hazzard =
        Hazzard.<HazzardType, Receiver>builder(HazzardType.TYPE_TOKEN)
            .viewerLookupServiceLocator(new ViewerLocatorResolver(), 1)
            .templateLocator((recv, key) -> {
              assertThat(key).isEqualTo("notification");
              return "Pling! You have a new mail from %mailAuthor%! Full mail: "
                  + email.examine(StringExaminer.simpleEscaping());
            })
            .<String, String>composed((recv, intermediate, placeholders, method, owner) -> {
              String result = intermediate;
              for (final Entry<String, ? extends String> entry : placeholders.entrySet()) {
                result = result.replace('%' + entry.getKey() + '%', entry.getValue());
              }
              return result;
            })
            .sent(Receiver::sendMessage)
            .variableResolver(
                new StandardTemplateVariableResolution<>(
                    new StandardSupertypeThenInterfaceSupertypeStrategy(true)))
            .weightedVariableResolver(String.class, new StringTemplateVariableResolver<>(),
                Integer.MIN_VALUE)
            .weightedVariableResolver(Mail.class, new MailTemplateVariableResolver<>(), 0)
            .create();

    hazzard.sendEmailNotification(receiver, email);

    verify(receiver)
        .sendMessage("Pling! You have a new mail from " + email.author() + "! Full mail: "
            + email.examine(StringExaminer.simpleEscaping()));
  }

  /* package-private */ interface HazzardType {
    TypeToken<HazzardType> TYPE_TOKEN = new TypeToken<>() {
    };

    @TranslationKey("notification")
    void sendEmailNotification(
        @Receive final Receiver receiver,
        @TemplateArgument final Mail mail
    );
  }

  @Target({ElementType.PARAMETER, ElementType.TYPE_USE})
  @Retention(RetentionPolicy.RUNTIME)
      /* package-private */ @interface Receive {
  }

  /* package-private */ static class ViewerLocatorResolver implements
          IViewerLookupServiceLocator<Receiver> {
    @Override
    public @Nullable IViewerLookupService<Receiver> resolve(final Method method, final Type proxy) {
      final Parameter[] parameters = method.getParameters();
      for (int i = 0; i < parameters.length; ++i) {
        final Parameter parameter = parameters[i];
        if (parameter.isAnnotationPresent(Receive.class)
            && Receiver.class.isAssignableFrom(parameter.getType())) {
          final int receiverParamIdx = i;
          return (calledMethod, calledProxy, providedParameters) -> (Receiver) providedParameters[receiverParamIdx];
        }
      }

      return null;
    }
  }

  /* package-private */ interface Receiver {
    void sendMessage(final String message);
  }

  /* package-private */ static class MockedReceiver implements Receiver {
    @Override
    public void sendMessage(final String message) {
      fail("must be mocked; got message: " + message);
    }
  }

  /* package-private */ static class StringTemplateVariableResolver<R> implements
          ITemplateVariableResolver<R, String, String> {
    @Override
    public @Nullable Map<String, VariableWrapper<ReplacementResult<? extends String>, IntermediateValue<?>>> resolve(
            final String variableName, final String value, final R viewer, final Type owner,
            final Method method, final @Nullable Object[] parameters) {
      return Map.of(variableName, VariableWrapper.finalResult(conclusionValue(value)));
    }
  }

  /* package-private */ interface Mail extends Examinable {
    String author();

    String title();

    String body();

    @Override
    @NonNull
    default Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.concat(Examinable.super.examinableProperties(), Stream.of(
          ExaminableProperty.of("author", this.author()),
          ExaminableProperty.of("title", this.title()),
          ExaminableProperty.of("body", this.body())
      ));
    }
  }

  /* package-private */ static class Email implements Mail {
    private final String author;
    private final String title;
    private final String body;

    Email(final String author, final String title, final String body) {
      this.author = author;
      this.title = title;
      this.body = body;
    }

    @Override
    public String author() {
      return this.author;
    }

    @Override
    public String title() {
      return this.title;
    }

    @Override
    public String body() {
      return this.body;
    }
  }

  /* package-private */ static class MailTemplateVariableResolver<R> implements
          ITemplateVariableResolver<R, Mail, String> {
    @Override
    public @Nullable Map<String, VariableWrapper<ReplacementResult<? extends String>, IntermediateValue<?>>> resolve(
            final String variableName, final Mail value, final R viewer, final Type owner,
            final Method method, final @Nullable Object[] parameters) {
      return Map.of(
          variableName + "Author", VariableWrapper.intermediateResult(continuanceValue(value.author(), String.class)),
          variableName + "Title", VariableWrapper.intermediateResult(continuanceValue(value.title(), String.class)),
          variableName + "Body", VariableWrapper.intermediateResult(continuanceValue(value.body(), String.class))
      );
    }
  }
}
