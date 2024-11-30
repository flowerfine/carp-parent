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

import cn.sliew.carp.framework.common.reflection.lambdas.JobRunrJob;
import cn.sliew.milky.common.exception.Rethrower;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.SerializedLambda;

import static java.util.Arrays.stream;

public class JavaJobDetailsFinder extends AbstractJobDetailsFinder {

    private final JobRunrJob jobRunrJob;
    private final SerializedLambda serializedLambda;
    private final boolean isLambda;

    JavaJobDetailsFinder(JobRunrJob jobRunrJob, SerializedLambda serializedLambda, Object... params) {
        super(new JavaJobDetailsBuilder(serializedLambda, params));
        this.jobRunrJob = jobRunrJob;
        this.serializedLambda = serializedLambda;
        this.isLambda = (serializedLambda.getImplMethodName().startsWith("lambda$") || serializedLambda.getImplMethodName().contains("$lambda-") || serializedLambda.getImplMethodName().contains("$lambda$"));
        if (isLambda) {
            try (InputStream classContainingLambdaInputStream = getClassContainingLambdaAsInputStream()) {
                parse(classContainingLambdaInputStream);
            } catch (IOException e) {
                Rethrower.throwAs(e);
            }
        } else if (serializedLambda.getCapturedArgCount() == 1 &&
                stream(serializedLambda.getCapturedArg(0).getClass().getAnnotations())
                        .anyMatch(ann -> "kotlin.Metadata".equals(ann.annotationType().getName()))) {
            // kotlin method reference
            this.jobDetailsBuilder.setClassName(serializedLambda.getCapturedArg(0).getClass().getName());
            this.jobDetailsBuilder.setMethodName(serializedLambda.getImplMethodName().contains("$") ?
                    StringUtils.substringAfter(serializedLambda.getImplMethodName(), "$")
                    : serializedLambda.getImplMethodName());
        }
    }

    @Override
    protected boolean isLambdaContainingJobDetails(String name) {
        return isLambda && name.equals(serializedLambda.getImplMethodName());
    }

    @Override
    protected InputStream getClassContainingLambdaAsInputStream() {
        return JobDetailsGeneratorUtils.getJavaClassContainingLambdaAsInputStream(jobRunrJob);
    }

}
