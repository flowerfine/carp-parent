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
import cn.sliew.carp.framework.common.util.reflection.ReflectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class VisitMethodInstruction extends AbstractJVMInstruction {

    protected String owner;
    protected String name;
    protected String descriptor;
    protected boolean isInterface;

    protected VisitMethodInstruction(JobDetailsBuilder jobDetailsBuilder) {
        super(jobDetailsBuilder);
    }

    public void load(String owner, String name, String descriptor, boolean isInterface) {
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
        this.isInterface = isInterface;
        jobDetailsBuilder.pushInstructionOnStack(this);
    }

    protected boolean isVoidInstruction() {
        return descriptor.endsWith(")V");
    }

    protected boolean isLastJobDetailsInstruction() {
        return jobDetailsBuilder.getInstructions().stream().noneMatch(JobDetailsInstruction.class::isInstance);
    }

    protected List<Object> getParametersUsingParamTypes(Class<?>[] paramTypesAsArray) {
        LinkedList<Class<?>> paramTypes = new LinkedList<>(Arrays.asList(paramTypesAsArray));
        List<Object> result = new ArrayList<>();
        while (!paramTypes.isEmpty()) {
            Class<?> paramType = paramTypes.pollLast();
            result.add(0, ReflectionUtils.autobox(jobDetailsBuilder.getStack().pollLast(), paramType));
        }
        return result;
    }

}
