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
import cn.sliew.carp.framework.common.reflection.lambdas.JobLambda;
import cn.sliew.carp.framework.common.reflection.lambdas.JobRunrJob;
import cn.sliew.carp.framework.common.util.reflection.SerializedLambdaUtil;

import java.lang.annotation.Annotation;

import static java.util.Arrays.stream;

public class JobDetailsAsmGenerator implements JobDetailsGenerator {

    @Override
    public JobDetails toJobDetails(JobLambda lambda) {
        if (isKotlinLambda(lambda)) {
            return new KotlinJobDetailsFinder(lambda).getJobDetails();
        } else {
            return new JavaJobDetailsFinder(lambda, SerializedLambdaUtil.toSerializedLambda(lambda)).getJobDetails();
        }
    }

    @Override
    public JobDetails toJobDetails(IocJobLambda lambda) {
        if (isKotlinLambda(lambda)) {
            return new KotlinJobDetailsFinder(lambda, new Object()).getJobDetails();
        } else {
            return new JavaJobDetailsFinder(lambda, SerializedLambdaUtil.toSerializedLambda(lambda)).getJobDetails();
        }
    }

    private <T extends JobRunrJob> boolean isKotlinLambda(T lambda) {
        return stream(lambda.getClass().getAnnotations()).map(Annotation::annotationType).anyMatch(annotationType -> annotationType.getName().equals("kotlin.Metadata"));
    }
}
