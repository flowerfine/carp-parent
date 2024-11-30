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
package cn.sliew.carp.framework.biz.ext.validation;

import cn.sliew.carp.framework.common.dict.DictDefinition;
import cn.sliew.carp.framework.common.dict.EnumDictRegistry;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;
import java.util.Objects;
import java.util.Optional;

@Documented
@Constraint(validatedBy = DictConstraint.DictValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DictConstraint {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String dict();

    class DictValidator implements ConstraintValidator<DictConstraint, String> {

        private DictConstraint constraint;

        @Override
        public void initialize(DictConstraint constraint) {
            this.constraint = constraint;
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (Objects.isNull(constraint.dict()) || Objects.isNull(value)) {
                context.buildConstraintViolationWithTemplate("[Dict Instance] is invalid").addConstraintViolation();
                return false;
            }

            Optional<DictDefinition> optional = EnumDictRegistry.INSTANCE.getDictDefinition(constraint.dict());
            if (optional.isEmpty()) {
                context.buildConstraintViolationWithTemplate("[Dict] is invalid").addConstraintViolation();
                return false;
            }
            if (EnumDictRegistry.INSTANCE.exists(optional.get(), value) == false) {
                context.buildConstraintViolationWithTemplate("[Dict Instance] is invalid").addConstraintViolation();
                return false;
            }
            return true;
        }
    }


}
