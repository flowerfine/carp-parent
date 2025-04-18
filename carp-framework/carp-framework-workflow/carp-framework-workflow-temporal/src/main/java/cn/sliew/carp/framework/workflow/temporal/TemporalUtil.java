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
package cn.sliew.carp.framework.workflow.temporal;

import cn.hutool.core.thread.ThreadUtil;
import cn.sliew.milky.common.exception.Rethrower;
import cn.sliew.milky.common.util.JacksonUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.activity.ActivityInfo;
import io.temporal.api.namespace.v1.NamespaceInfo;
import io.temporal.api.workflowservice.v1.DescribeNamespaceResponse;
import io.temporal.api.workflowservice.v1.ListNamespacesRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.schedules.ScheduleClient;
import io.temporal.client.schedules.ScheduleClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.workflow.WorkflowInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toSet;

@Slf4j
public enum TemporalUtil {
    ;

    private static Map<String, WorkflowServiceStubs> workflowServiceStubsCache = new HashMap<>();

    private static final LoadingCache<String, WorkflowClient> workflowClientCache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(30L))
            .build(new CacheLoader<>() {
                @Override
                public WorkflowClient load(String key) throws Exception {
                    String[] split = StringUtils.split(key, "-");
                    return TemporalUtil.createWorkflowClient(split[0], split[1]);
                }
            });

    private static final LoadingCache<String, ScheduleClient> scheduleClientCache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(30L))
            .build(new CacheLoader<>() {
                @Override
                public ScheduleClient load(String key) throws Exception {
                    String[] split = StringUtils.split(key, "-");
                    return TemporalUtil.createScheduleClient(split[0], split[1]);
                }
            });

    public static final String[] DO_NOT_RETRYS = new String[]{
            "java.util.concurrent.TimeoutException"
    };

    public static final String DEFAULT_NAMESPACE = "default";

    public static WorkflowClient getWorkflowClient(String host, String namespace) {
        try {
            return workflowClientCache.get(host + "-" + namespace);
        } catch (ExecutionException e) {
            log.error("Get Temporal WorkflowClient error, host: {}, namespace: {}",
                    host, namespace);
            Rethrower.throwAs(e);
            return null;
        }
    }

    public static ScheduleClient getScheduleClient(String host, String namespace) {
        try {
            return scheduleClientCache.get(host + "-" + namespace);
        } catch (ExecutionException e) {
            log.error("Get Temporal ScheduleClient error, host: {}, namespace: {}",
                    host, namespace);
            Rethrower.throwAs(e);
            return null;
        }
    }

    private static WorkflowClient createWorkflowClient(final String temporalHost) {
        return createWorkflowClient(temporalHost, DEFAULT_NAMESPACE);
    }

    private static WorkflowClient createWorkflowClient(final String temporalHost, final String namespace) {
        WorkflowServiceStubs service = TemporalUtil.getTemporalService(temporalHost, namespace);
        WorkflowClientOptions workflowClientOptions = WorkflowClientOptions.newBuilder()
                .setNamespace(namespace)
                .validateAndBuildWithDefaults();
        return WorkflowClient.newInstance(service, workflowClientOptions);
    }

    private static ScheduleClient createScheduleClient(final String temporalHost) {
        return createScheduleClient(temporalHost, DEFAULT_NAMESPACE);
    }

    private static ScheduleClient createScheduleClient(final String temporalHost, final String namespace) {
        WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(temporalHost)
                .setEnableKeepAlive(true)
                .setKeepAliveTime(Duration.ofMinutes(1L))
                .setKeepAliveTimeout(Duration.ofMinutes(5L))
                .build();
        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance(options);

        ScheduleClientOptions scheduleClientOptions = ScheduleClientOptions.newBuilder()
                .setNamespace(namespace)
                .build();
        return ScheduleClient.newInstance(service, scheduleClientOptions);
    }

    private static WorkflowServiceStubs getTemporalService(final String temporalHost, final String namespace) {
        return workflowServiceStubsCache.computeIfAbsent(temporalHost, key -> createTemporalService(key, namespace));
    }

    private static WorkflowServiceStubs createTemporalService(final String temporalHost, final String namespace) {
        final WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(temporalHost)
                .setEnableKeepAlive(true)
                .setKeepAliveTime(Duration.ofMinutes(1L))
                .setKeepAliveTimeout(Duration.ofMinutes(5L))
                .build();

        return getTemporalClientWhenConnected(
                namespace,
                Duration.ofSeconds(2),
                Duration.ofMinutes(2),
                Duration.ofSeconds(10),
                () -> WorkflowServiceStubs.newInstance(options));
    }

    /**
     * Loops and waits for the Temporal service to become available and returns a client.
     * <p>
     * This function uses a supplier as input since the creation of a WorkflowServiceStubs can result in
     * connection exceptions as well.
     */
    private static WorkflowServiceStubs getTemporalClientWhenConnected(
            final String namespace,
            final Duration waitInterval,
            final Duration maxTimeToConnect,
            final Duration waitAfterConnection,
            final Supplier<WorkflowServiceStubs> temporalServiceSupplier) {
        log.info("Waiting for temporal server...");

        boolean temporalStatus = false;
        WorkflowServiceStubs temporalService = null;
        long millisWaited = 0;

        while (!temporalStatus) {
            if (millisWaited >= maxTimeToConnect.toMillis()) {
                throw new RuntimeException("Could not create Temporal client within max timeout!");
            }

            log.warn("Waiting for namespace {} to be initialized in temporal...", namespace);
            ThreadUtil.sleep(waitInterval.toMillis());
            millisWaited = millisWaited + waitInterval.toMillis();

            try {
                temporalService = temporalServiceSupplier.get();
                temporalStatus = getNamespaces(temporalService).contains(namespace);
            } catch (final Exception e) {
                // Ignore the exception because this likely means that the Temporal service is still initializing.
                log.warn("Ignoring exception while trying to request Temporal namespaces:", e);
            }
        }

        // sometimes it takes a few additional seconds for workflow queue listening to be available
        ThreadUtil.sleep(waitAfterConnection.toMillis());

        log.info("Found temporal namespace {}!", namespace);

        return temporalService;
    }

    private static Set<String> getNamespaces(final WorkflowServiceStubs temporalService) {
        return temporalService.blockingStub()
                .listNamespaces(ListNamespacesRequest.newBuilder().build())
                .getNamespacesList()
                .stream()
                .map(DescribeNamespaceResponse::getNamespaceInfo)
                .map(NamespaceInfo::getName)
                .collect(toSet());
    }

    public static void logWorkflow(Logger log, WorkflowInfo workflowInfo, Object... params) {
        log.debug("执行工作流! namespace: {}, workflowType: {}, taskQueue: {}, workflowId: {}, runId: {}, param: {}",
                workflowInfo.getNamespace(), workflowInfo.getWorkflowType(), workflowInfo.getTaskQueue(),
                workflowInfo.getWorkflowId(), workflowInfo.getRunId(), JacksonUtil.toJsonString(Arrays.asList(params)));
    }

    public static void logActivityContext(Logger log, ActivityExecutionContext context, Object... params) {
        ActivityInfo info = context.getInfo();
        log.debug("执行 activity! workflowNamespace: {}, workflowType: {}," +
                        " workflowId: {}, runId: {}, activityId: {}, " +
                        "activityNamespace: {}, activityType: {}, param: {}",
                info.getWorkflowNamespace(),
                info.getWorkflowType(),
                info.getWorkflowId(),
                info.getRunId(),
                info.getActivityId(),
                info.getActivityNamespace(),
                info.getActivityType(),
                JacksonUtil.toJsonString(Arrays.asList(params))
        );
    }
}
