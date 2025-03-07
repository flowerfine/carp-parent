/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.sliew.carp.framework.common.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;

public enum ValidatorUtil {
    ;

    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public static <T> ValidateResult validate(T object) {
        Set<ConstraintViolation<T>> validate = validator.validate(object);
        if (validate.isEmpty()) {
            return ValidateResult.success();
        }

        ValidateResult.ValidateResultBuilder builder = ValidateResult.builder().valid(false);
        for (ConstraintViolation constraintViolation : validate) {
            builder.rootBeanClass(constraintViolation.getRootBeanClass().getSimpleName())
                    .rootBean(constraintViolation.getRootBean());

            ValidateResultItem item = ValidateResultItem.builder()
                    .property(constraintViolation.getPropertyPath().toString())
                    .invalidValue(constraintViolation.getInvalidValue())
                    .message(constraintViolation.getMessage())
                    .build();
            builder.item(item);
        }
        return builder.build();
    }

}
