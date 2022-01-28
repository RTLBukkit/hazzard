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
package net.kyori.hazzard.annotation;

import net.kyori.hazzard.strategy.StandardTemplateVariableResolution;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for a template variable for detection with {@link StandardTemplateVariableResolution}.
 */
@Documented
@Target({ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TemplateArgument {
  /**
   * The name of the template argument. If empty, the resolver will use parameter name.
   *
   * @return the name of the template argument
   */
  String value() default "";
}
