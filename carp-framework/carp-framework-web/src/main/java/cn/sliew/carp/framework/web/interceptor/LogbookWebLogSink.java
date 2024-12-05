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
import cn.sliew.carp.framework.log.model.LogRecord;
import cn.sliew.carp.framework.log.model.LogRequest;
import cn.sliew.carp.framework.log.model.LogResponse;
import cn.sliew.carp.framework.log.service.CarpSystemLogActionService;
import cn.sliew.milky.common.util.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.logbook.*;

import java.io.IOException;
import java.util.Date;

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
        LogRecord record = new LogRecord();
        record.setStartTime(Date.from(correlation.getStart()));
        record.setEndTime(Date.from(correlation.getEnd()));
        record.setDuration(DateUtil.betweenMs(record.getStartTime(), record.getEndTime()));

        LogRequest logRequest = new LogRequest();
        logRequest.setMethod(request.getMethod());
        logRequest.setPath(request.getPath());
        logRequest.setParam(request.getQuery());
        logRequest.setBody(request.getBodyAsString());
        logRequest.setHeaders(request.getHeaders());
        logRequest.setIp(request.getHost());
        record.setRequest(logRequest);

        LogResponse logResponse = new LogResponse();
        logResponse.setStatus(response.getStatus());
        logResponse.setBody(response.getBodyAsString());
        record.setResponse(logResponse);
        log.info("{}", JacksonUtil.toJsonString(record));
    }

}
