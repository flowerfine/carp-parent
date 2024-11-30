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
package cn.sliew.carp.framework.common.reflection.instructions;

import cn.sliew.carp.framework.common.reflection.JobDetailsBuilder;
import cn.sliew.carp.framework.common.reflection.JobDetailsGeneratorUtils;
import cn.sliew.carp.framework.common.util.reflection.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static java.util.Arrays.stream;

public class InvokeSpecialInstruction extends VisitMethodInstruction {

    public InvokeSpecialInstruction(JobDetailsBuilder jobDetailsBuilder) {
        super(jobDetailsBuilder);
    }

    @Override
    public Object invokeInstruction() {
        if ("<init>".equals(name)) {
            String className = JobDetailsGeneratorUtils.toFQClassName(owner);
            Class<?>[] paramTypes = JobDetailsGeneratorUtils.findParamTypesFromDescriptorAsArray(descriptor);
            List<Object> parameters = getParametersUsingParamTypes(paramTypes);

            Object objectViaConstructor = JobDetailsGeneratorUtils.createObjectViaConstructor(className, paramTypes, parameters.toArray());
            if (isKotlinMethodReference(objectViaConstructor)) {
                jobDetailsBuilder.setClassName(((Class) ReflectionUtils.getValueFromFieldOrProperty(objectViaConstructor, "owner")).getName());
                jobDetailsBuilder.setMethodName((String) ReflectionUtils.getValueFromFieldOrProperty(objectViaConstructor, "name"));
            }
            return objectViaConstructor;
        }

        String className = JobDetailsGeneratorUtils.toFQClassName(owner);
        Class<?> objectClass = ReflectionUtils.toClass(className);
        Method method = ReflectionUtils.getMethod(objectClass, name, JobDetailsGeneratorUtils.findParamTypesFromDescriptorAsArray(descriptor));
        if (Modifier.isPrivate(method.getModifiers())) {
            throw new RuntimeException(String.format("JobRunr cannot access member \"%s\" of class %s with modifiers \"private\". Please make the method \"public\".", name, className));
        }

        throw new IllegalStateException("Unknown INVOKESPECIAL instruction: " + className + "." + name);
    }

    private boolean isKotlinMethodReference(Object objectViaConstructor) {
        return stream(objectViaConstructor.getClass().getInterfaces())
                .map(Class::getName)
                .anyMatch(name -> name.startsWith("kotlin.jvm.functions.Function"));
    }

}
