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

import cn.sliew.carp.framework.common.reflection.instructions.AbstractJVMInstruction;
import cn.sliew.carp.framework.common.reflection.postprocess.CGLibPostProcessor;
import cn.sliew.carp.framework.common.reflection.postprocess.JobDetailsPostProcessor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.singletonList;

public abstract class JobDetailsBuilder {

    private final LinkedList<AbstractJVMInstruction> instructions;
    private final LinkedList<Object> stack;
    private final List<Object> localVariables;
    private String jobDetailsClassName;
    private String jobDetailsStaticFieldName;
    private String jobDetailsMethodName;
    private List<JobParameter> jobDetailsJobParameters;
    private List<JobDetailsPostProcessor> jobDetailsPostProcessors;

    protected JobDetailsBuilder(List<Object> localVariables) {
        this(localVariables, null, null);
    }

    protected JobDetailsBuilder(List<Object> localVariables, String className, String methodName) {
        this.instructions = new LinkedList<>();
        this.stack = new LinkedList<>();
        this.localVariables = localVariables;

        setClassName(className);
        setMethodName(methodName);
        setJobParameters(new ArrayList<>());
        jobDetailsPostProcessors = singletonList(new CGLibPostProcessor());
    }

    public void pushInstructionOnStack(AbstractJVMInstruction jvmInstruction) {
        instructions.add(jvmInstruction);
    }

    public Object getLocalVariable(int nbrInStack) {
        if (nbrInStack < localVariables.size()) {
            return localVariables.get(nbrInStack);
        }
        throw new IllegalStateException("Can not find variable " + nbrInStack + " in stack");
    }

    public void addLocalVariable(Object o) {
        this.localVariables.add(o);
    }

    public List<AbstractJVMInstruction> getInstructions() {
        return instructions;
    }

    public AbstractJVMInstruction pollFirstInstruction() {
        return instructions.pollFirst();
    }

    public LinkedList<Object> getStack() {
        return stack;
    }

    public JobDetails getJobDetails() {
        invokeInstructions();
        final JobDetails jobDetails = new JobDetails(jobDetailsClassName, jobDetailsStaticFieldName, jobDetailsMethodName, jobDetailsJobParameters);
        return postProcessJobDetails(jobDetails);
    }

    private JobDetails postProcessJobDetails(JobDetails jobDetails) {
        JobDetails currentJobDetails = jobDetails;
        for (JobDetailsPostProcessor postProcessor : getJobDetailsPostProcessors()) {
            currentJobDetails = postProcessor.postProcess(currentJobDetails);
        }
        return currentJobDetails;
    }

    private void invokeInstructions() {
        if (instructions.isEmpty() && localVariables.size() > 1) { // it is a method reference
            for (int i = 1; i < localVariables.size(); i++) {
                Object variable = localVariables.get(i);
                jobDetailsJobParameters.add(new JobParameter(variable));
            }
        } else {
            AbstractJVMInstruction instruction = pollFirstInstruction();
            while (instruction != null) {
                instruction.invokeInstructionAndPushOnStack();
                instruction = pollFirstInstruction();
            }
        }
    }

    public void setClassName(String className) {
        if (jobDetailsStaticFieldName == null) {
            jobDetailsClassName = className;
        }
    }

    public void setStaticFieldName(String name) {
        jobDetailsStaticFieldName = name;
    }

    public void setMethodName(String name) {
        jobDetailsMethodName = name;
    }

    public void setJobParameters(List<JobParameter> jobParameters) {
        jobDetailsJobParameters = jobParameters;
    }

    List<JobDetailsPostProcessor> getJobDetailsPostProcessors() {
        return jobDetailsPostProcessors;
    }
}
