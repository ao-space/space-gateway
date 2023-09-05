/*
 * Copyright (c) 2022 Institute of Software Chinese Academy of Sciences (ISCAS)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.ao.services.support.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation used to validate the constraint that element
 * is any of specified enum class values.
 *
 * @since 1.0
 * @see ValueOfEnum
 * @author Haibo Luo
 */
public class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, Object> {

  /*
   * Implementation note:
   *
   * This implementation is inspired by following discussion and article
   * 1. https://www.baeldung.com/javax-validations-enums
   * 2. https://stackoverflow.com/questions/18205787/how-to-use-hibernate-validation-annotations-with-enums
   * 3. https://quarkus.io/guides/validation
   */

  /**
   * Used to hold all values of enum class.
   */
  private Collection<Object> enumValues;

  @SneakyThrows
  @Override
  public void initialize(ValueOfEnum annotation) {
    final Method method = annotation.enumClass().getMethod(annotation.valueMethod());
    enumValues = Stream.of(annotation.enumClass().getEnumConstants())
        .map(e -> invokeValueMethod(method, e))
        .collect(Collectors.toUnmodifiableSet());
  }

  @SneakyThrows
  private static Object invokeValueMethod(Method method, Enum<?> e) {
    return method.invoke(e);
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    return (value == null) || enumValues.contains(value);
  }
}