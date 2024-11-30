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
import org.objectweb.asm.Handle;

import java.util.List;

public class InvokeDynamicInstruction extends AbstractJVMInstruction {

    private String name;
    private String descriptor;
    private Handle bootstrapMethodHandle;
    private Object[] bootstrapMethodArguments;

    public InvokeDynamicInstruction(JobDetailsBuilder jobDetailsBuilder) {
        super(jobDetailsBuilder);
    }

    public void load(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        jobDetailsBuilder.pushInstructionOnStack(this);
        this.name = name;
        this.descriptor = descriptor;
        this.bootstrapMethodHandle = bootstrapMethodHandle;
        this.bootstrapMethodArguments = bootstrapMethodArguments;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public Handle getBootstrapMethodHandle() {
        return bootstrapMethodHandle;
    }

    public Object[] getBootstrapMethodArguments() {
        return bootstrapMethodArguments;
    }

    @Override
    public Object invokeInstruction() {
        if ("makeConcatWithConstants".equals(name)) {
            String result = bootstrapMethodArguments[0].toString();
            final List<Class<?>> paramTypes = JobDetailsGeneratorUtils.findParamTypesFromDescriptor(descriptor);
            while (result.contains("\u0001") && !paramTypes.isEmpty()) {
                final Class<?> paramType = paramTypes.remove(paramTypes.size() - 1);
                final Object value = ReflectionUtils.autobox(jobDetailsBuilder.getStack().pollLast(), paramType);
                result = replaceLast(result, "\u0001", value.toString());
            }
            return result;
        }
        throw new IllegalStateException("Unknown INVOKEDYNAMIC instruction: " + name);
    }

    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
    }
}
