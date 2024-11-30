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
package cn.sliew.carp.framework.common.util.reflection;

import cn.sliew.carp.framework.common.reflection.JobDetails;
import cn.sliew.carp.framework.common.reflection.JobDetailsAsmGenerator;
import cn.sliew.carp.framework.common.reflection.JobDetailsGenerator;
import cn.sliew.carp.framework.common.reflection.lambdas.JobLambda;
import cn.sliew.milky.common.exception.Rethrower;
import cn.sliew.milky.common.util.JacksonUtil;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

public enum SerializedLambdaUtil {
    ;

    public static <T> SerializedLambda toSerializedLambda(T value) {
        if (!value.getClass().isSynthetic()) {
            throw new IllegalArgumentException("support lambda expression only");
        }

        if (!(value instanceof Serializable)) {
            throw new RuntimeException("lambda must be Serializable");
        }

        try {
            Method writeReplaceMethod = value.getClass().getDeclaredMethod("writeReplace");
            ReflectionUtils.makeAccessible(writeReplaceMethod);
            return (SerializedLambda) writeReplaceMethod.invoke(value);
        } catch (Exception ignored) {
            Rethrower.throwAs(ignored);
            return null;
        }
    }

    public static String toFQClassName(String byteCodeName) {
        return byteCodeName.replace("/", ".");
    }

    public static String toFQResource(String byteCodeName) {
        return byteCodeName.replace(".", "/");
    }

    public static InputStream getLambdaDeclaringClassAsInputStream(SerializedLambda lambda) {
        String location = getClassLocationOfLambda(lambda);
        return lambda.getClass().getResourceAsStream(location);
    }

    public static String getClassLocationOfLambda(SerializedLambda lambda) {
        String name = lambda.getImplClass();
        return "/" + toFQResource(name.substring(0, name.indexOf("$$"))) + ".class";
    }

    public static SerializedLambda testLambda(JobLambda invocation) {
        return toSerializedLambda(invocation);
    }

    public static JobDetails testJobDetails(JobLambda invocation) {
        JobDetailsGenerator generator = new JobDetailsAsmGenerator();
        return generator.toJobDetails(invocation);
    }

    public static void main(String[] args) {
        testJobDetails();
//        testSerializedLambda();
    }

    private static void testJobDetails() {
        String echo = "hello, lambda";
        JobDetails jobDetails = testJobDetails(() -> System.out.println(echo));
        System.out.println(JacksonUtil.toJsonString(jobDetails));
    }

    private static void testSerializedLambda() {
        String echo = "hello, lambda";
        SerializedLambda serializedLambda = testLambda(() -> System.out.println(echo));

        // cn/sliew/carp/framework/common/util/reflection/SerializedLambdaUtil
        System.out.println(serializedLambda.getImplClass());
        // lambda$main$d04c9b6f$1
        System.out.println(serializedLambda.getImplMethodName());
        // 6
        System.out.println(serializedLambda.getImplMethodKind());
        // string -> void
        System.out.println(serializedLambda.getImplMethodSignature());

        // cn/sliew/carp/framework/common/rpc/invocation/LambdaInvocation
        System.out.println(serializedLambda.getFunctionalInterfaceClass());
        // run
        System.out.println(serializedLambda.getFunctionalInterfaceMethodName());
        // () -> void
        System.out.println(serializedLambda.getFunctionalInterfaceMethodSignature());

        // cn/sliew/carp/framework/common/util/reflection/SerializedLambdaUtil
        System.out.println(serializedLambda.getCapturingClass());
        int capturedArgCount = serializedLambda.getCapturedArgCount();
        for (int i = 0; i < capturedArgCount; i++) {
            // hello, lambda
            System.out.println(serializedLambda.getCapturedArg(i));
        }
    }
}
