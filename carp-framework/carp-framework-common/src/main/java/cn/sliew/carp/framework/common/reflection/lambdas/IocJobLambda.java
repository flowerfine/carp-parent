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
package cn.sliew.carp.framework.common.reflection.lambdas;

/**
 * This is a functional interface which represents a lambda that will be parsed by JobRunr.
 * You <strong>may not create an actual instance of this class</strong>, instead you use it as follows:
 *
 * <pre>{@code
 *     BackgroundJob.<SomeService>enqueue(x -> x.doWork("some argument"))
 * }</pre>
 * <p>
 * or
 * <pre>{@code
 *     jobScheduler.<SomeService>enqueue(x -> x.doWork("some argument"))
 * }</pre>
 * <p>
 * This functional interface allows you to enqueue background jobs without having an actual instance available of your service.
 * While processing, JobRunr will lookup the actual service in the IoC container or create a new instance using the default constructor.
 *
 * @param <S> Your service on which you want to call a background job method.
 */
@FunctionalInterface
public interface IocJobLambda<S> extends JobRunrJob {

    void accept(S service) throws Exception;
}
