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
package cn.sliew.carp.framework.web.interceptor;

import cn.hutool.core.date.DateUtil;
import cn.sliew.carp.framework.common.security.CarpSecurityContext;
import cn.sliew.carp.framework.common.security.OnlineUserInfo;
import cn.sliew.carp.framework.log.web.enums.LogEntity;
import cn.sliew.carp.framework.log.web.model.LogRecord;
import cn.sliew.carp.framework.log.web.model.LogRequest;
import cn.sliew.carp.framework.log.web.model.LogResponse;
import cn.sliew.carp.framework.log.web.model.UserInfo;
import cn.sliew.carp.framework.log.web.service.CarpSystemLogActionService;
import cn.sliew.carp.framework.web.util.RequestParamUtil;
import cn.sliew.milky.common.util.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.zalando.logbook.*;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class LogbookWebLogSink implements Sink {

    @Autowired
    private CarpSystemLogActionService carpSystemLogActionService;

    @Override
    public void write(Precorrelation precorrelation, HttpRequest request) throws IOException {

    }

    @Override
    public void write(Correlation correlation, HttpRequest request, HttpResponse response) throws IOException {
        try {
            printLog(correlation, request, response);
        } catch (Exception e) {
            log.error("Log exceptions", e);
        }
    }

    public void printLog(final Correlation correlation, final HttpRequest request, final HttpResponse response) throws IOException {
        Optional<HandlerMethod> optional = RequestParamUtil.getHandlerMethod();

        if (optional.isEmpty()) {
            return;
        }

        HandlerMethod handlerMethod = optional.get();

        LogRecord record = new LogRecord();
        Pair<String, String> pair = RequestParamUtil.findModuleAndDesc(handlerMethod);
        record.setModule(pair.getLeft());
        record.setDesc(pair.getRight());

        record.setStartTime(Date.from(correlation.getStart()));
        record.setEndTime(Date.from(correlation.getEnd()));
        record.setDuration(DateUtil.betweenMs(record.getStartTime(), record.getEndTime()));

        UserInfo userInfo = new UserInfo();
        OnlineUserInfo onlineUserInfo = CarpSecurityContext.get();
        if (Objects.nonNull(onlineUserInfo)) {
            userInfo.setUserId(onlineUserInfo.getUserId().toString());
            userInfo.setUserName(onlineUserInfo.getUserName());
            userInfo.setNickName(onlineUserInfo.getNickName());
        }
        record.setUser(userInfo);

        LogRequest logRequest = new LogRequest();
        record.setRequest(logRequest);
        logRequest.setMethod(request.getMethod());
        logRequest.setPath(request.getPath());
        logRequest.setIp(request.getHost());

        LogResponse logResponse = new LogResponse();
        record.setResponse(logResponse);
        logResponse.setStatus(response.getStatus());
        Set<LogEntity> logEntities = RequestParamUtil.findLogEntry(handlerMethod);
        if (CollectionUtils.isEmpty(logEntities) == false) {
            if (logEntities.contains(LogEntity.HTTP_REQUEST_HEADERS)) {
                logRequest.setHeaders(request.getHeaders());
            }
            if (logEntities.contains(LogEntity.HTTP_RESPONSE_BODY)) {
                logRequest.setBody(request.getBodyAsString());
            }
            if (logEntities.contains(LogEntity.HTTP_REQUEST_PARAM)) {
                logRequest.setParam(request.getQuery());
            }
            if (logEntities.contains(LogEntity.HTTP_RESPONSE_BODY)) {
                logResponse.setBody(response.getBodyAsString());
            }
        }

        log.info("{}", JacksonUtil.toJsonString(record));
    }

}
