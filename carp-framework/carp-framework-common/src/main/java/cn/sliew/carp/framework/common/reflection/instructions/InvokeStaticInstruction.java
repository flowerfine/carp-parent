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

import java.util.List;

public class InvokeStaticInstruction extends JobDetailsInstruction {

    public InvokeStaticInstruction(JobDetailsBuilder jobDetailsBuilder) {
        super(jobDetailsBuilder);
    }

    @Override
    public Object invokeInstruction() {
        if (isKotlinNullCheck()) {
            getObject();
            return DO_NOT_PUT_ON_STACK;
        }
        return super.invokeInstruction();
    }

    protected Object getObject() {
        Class<?>[] paramTypes = JobDetailsGeneratorUtils.findParamTypesFromDescriptorAsArray(descriptor);
        List<Object> parameters = getParametersUsingParamTypes(paramTypes);
        if (isKotlinNullCheck()) return null;
        Object result = JobDetailsGeneratorUtils.createObjectViaStaticMethod(getClassName(), getMethodName(), paramTypes, parameters.toArray());
        return result;
    }

    private boolean isKotlinNullCheck() {
        return getClassName().startsWith("kotlin.") && (getMethodName().startsWith("checkNotNull"));
    }

    String getClassName() {
        return JobDetailsGeneratorUtils.toFQClassName(owner);
    }
}
