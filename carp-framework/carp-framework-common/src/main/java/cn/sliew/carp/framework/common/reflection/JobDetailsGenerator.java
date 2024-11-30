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

public interface JobDetailsGenerator {

    JobDetails toJobDetails(JobLambda lambda);

    JobDetails toJobDetails(IocJobLambda<?> lambda);

    default JobDetails toJobDetails(JobRunrJob jobRunrJob) {
        if(jobRunrJob instanceof JobLambda) {
            return toJobDetails((JobLambda) jobRunrJob);
        } else if(jobRunrJob instanceof IocJobLambda) {
            return toJobDetails((IocJobLambda) jobRunrJob);
        }
        throw new IllegalArgumentException("The provided JobRunr job is not a valid JobRunr Job.");
    }
}
