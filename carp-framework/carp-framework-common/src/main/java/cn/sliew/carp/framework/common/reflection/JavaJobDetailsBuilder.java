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
package cn.sliew.carp.framework.common.reflection;

import cn.sliew.carp.framework.common.reflection.lambdas.IocJobLambda;

import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class JavaJobDetailsBuilder extends JobDetailsBuilder {

    public JavaJobDetailsBuilder(SerializedLambda serializedLambda, Object... params) {
        super(initLocalVariables(serializedLambda, params), JobDetailsGeneratorUtils.toFQClassName(serializedLambda.getImplClass()), serializedLambda.getImplMethodName());
    }

    protected static List<Object> initLocalVariables(SerializedLambda serializedLambda, Object[] params) {
        List<Object> result = new ArrayList<>();
        final Class<?>[] paramTypesFromDescriptor = JobDetailsGeneratorUtils.findParamTypesFromDescriptorAsArray(serializedLambda.getImplMethodSignature());
        final boolean isIoCJobLambda = IocJobLambda.class.getName().equals(JobDetailsGeneratorUtils.toFQClassName(serializedLambda.getFunctionalInterfaceClass()));
        for (int i = 0; i < serializedLambda.getCapturedArgCount(); i++) {
            final Object capturedArg = serializedLambda.getCapturedArg(i);
            result.add(capturedArg);
            if (isPrimitiveLongOrDouble(serializedLambda.getImplMethodKind(), paramTypesFromDescriptor, i, capturedArg)) { //why: If the local variable at index is of type double or long, it occupies both index and index + 1. See https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html
                result.add(null);
            }
        }
        result.addAll(asList(params));
        if (isIoCJobLambda) {
            result.add(null); // will be injected by IoC
        }
        return result;
    }

    private static boolean isPrimitiveLongOrDouble(int implMethodKind, Class<?>[] paramTypesFromDescriptor, int captureArgCounter, Object capturedArg) {
        if (MethodHandleInfo.REF_invokeStatic == implMethodKind) {
            return isPrimitiveLongOrDouble(paramTypesFromDescriptor[captureArgCounter], capturedArg);
        } else if (MethodHandleInfo.REF_invokeVirtual == implMethodKind || MethodHandleInfo.REF_invokeSpecial == implMethodKind || MethodHandleInfo.REF_invokeInterface == implMethodKind) {
            return captureArgCounter > 0 && isPrimitiveLongOrDouble(paramTypesFromDescriptor[captureArgCounter - 1], capturedArg);
        }
        return false;
    }

    private static boolean isPrimitiveLongOrDouble(Class<?> paramTypeFromDescriptor, Object capturedArg) {
        return paramTypeFromDescriptor.isPrimitive() && (capturedArg instanceof Long || capturedArg instanceof Double);
    }
}
