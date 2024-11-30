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

import cn.sliew.carp.framework.common.util.reflection.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class JobDetails {

    private final String className;
    private final String staticFieldName;
    private final String methodName;
    private final ArrayList<JobParameter> jobParameters;
    private Boolean cacheable;

    private JobDetails() {
        this(null, null, null, null);
        // used for deserialization
    }

    public JobDetails(String className, String staticFieldName, String methodName, List<JobParameter> jobParameters) {
        this.className = className;
        this.staticFieldName = staticFieldName;
        this.methodName = methodName;
        this.jobParameters = new ArrayList<>(jobParameters);
        this.cacheable = false;
    }

    public String getClassName() {
        return className;
    }

    public String getStaticFieldName() {
        return staticFieldName;
    }

    public boolean hasStaticFieldName() {
        return staticFieldName != null;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<JobParameter> getJobParameters() {
        return unmodifiableList(jobParameters);
    }

    public Class[] getJobParameterTypes() {
        return jobParameters.stream()
                .map(JobParameter::getClassName)
                .map(ReflectionUtils::toClass)
                .toArray(Class[]::new);
    }

    public Object[] getJobParameterValues() {
        return jobParameters.stream()
                .map(JobParameter::getObject)
                .toArray();
    }

    public Boolean getCacheable() {
        return cacheable;
    }

    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }
}
