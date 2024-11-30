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
import cn.sliew.carp.framework.common.reflection.JobParameter;
import cn.sliew.carp.framework.common.util.reflection.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;

import static cn.sliew.carp.framework.common.util.reflection.ReflectionUtils.toClass;

public class JobDetailsInstruction extends VisitMethodInstruction {

    public JobDetailsInstruction(JobDetailsBuilder jobDetailsBuilder) {
        super(jobDetailsBuilder);
    }

    @Override
    public Object invokeInstruction() {
        if (!isLastJobDetailsInstruction() && isVoidInstruction()) {
            throw new IllegalStateException("JobRunr only supports enqueueing/scheduling of one method");
        } else if (isLastJobDetailsInstruction()) {
            jobDetailsBuilder.setClassName(getClassName());
            jobDetailsBuilder.setMethodName(getMethodName());
            jobDetailsBuilder.setJobParameters(getJobParameters());
            return null;
        } else if (owner.startsWith("java")) {
            return getObject();
        } else {
            return getObject();
        }
    }

    String getClassName() {
        String className = JobDetailsGeneratorUtils.toFQClassName(owner);
        if (jobDetailsBuilder.getStack().isEmpty()) {
            return findInheritedClassName(className).orElse(className);
        }

        ListIterator objectOnStackIterator = jobDetailsBuilder.getStack().listIterator(jobDetailsBuilder.getStack().size());
        while (objectOnStackIterator.hasPrevious()) {
            Object jobOnStack = objectOnStackIterator.previous();
            if (jobOnStack != null && !jobOnStack.getClass().isSynthetic() && !Proxy.isProxyClass(jobOnStack.getClass())) {
                Class<Object> jobClass = ReflectionUtils.toClass(className);
                if (jobClass.isAssignableFrom(jobOnStack.getClass())) {
                    return jobOnStack.getClass().getName();
                }
            }
        }
        return className;
    }


    String getMethodName() {
        return name;
    }

    protected Object getObject() {
        Class<?>[] paramTypes = JobDetailsGeneratorUtils.findParamTypesFromDescriptorAsArray(descriptor);
        final Object ownerObject = jobDetailsBuilder.getStack().remove(jobDetailsBuilder.getStack().size() - 1 - paramTypes.length);
        return JobDetailsGeneratorUtils.createObjectViaMethod(ownerObject, name, paramTypes, getParametersUsingParamTypes(paramTypes).toArray());
    }

    private Optional<String> findInheritedClassName(String className) {
        if (jobDetailsBuilder.getLocalVariable(0) != null && jobDetailsBuilder.getLocalVariable(0).getClass().getDeclaredFields().length > 0) {
            final Field declaredField = jobDetailsBuilder.getLocalVariable(0).getClass().getDeclaredFields()[0];
            final Object valueFromField = ReflectionUtils.getValueFromField(declaredField, jobDetailsBuilder.getLocalVariable(0));
            if (toClass(className).isAssignableFrom(valueFromField.getClass())) {
                return Optional.of(valueFromField.getClass().getName());
            }
        }
        return Optional.empty();
    }

    protected List<JobParameter> getJobParameters() {
        final List<Class<?>> paramTypesFromDescriptor = JobDetailsGeneratorUtils.findParamTypesFromDescriptor(descriptor);
        final LinkedList<Class<?>> paramTypes = new LinkedList<>(paramTypesFromDescriptor);

        List<JobParameter> result = new ArrayList<>();
        while (!paramTypes.isEmpty()) {
            result.add(0, toJobParameter(paramTypes.pollLast(), jobDetailsBuilder.getStack().pollLast()));
        }
        return result;
    }

    private JobParameter toJobParameter(Class<?> paramType, Object param) {
        if (param == null) {
            throw new NullPointerException("You are passing null as a parameter to your background job for type " + paramType.getName() + " - JobRunr prevents this to fail fast.");
        }

        if (ReflectionUtils.isClassAssignableToObject(paramType, param)) {
            if (boolean.class.equals(paramType) && Integer.class.equals(param.getClass()))
                return new JobParameter(paramType, ((Integer) param) > 0);
            return new JobParameter(paramType, param);
        } else {
            throw new IllegalStateException("The found parameter types do not match the parameters.");
        }
    }
}
