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

import cn.sliew.carp.framework.common.security.CarpSecurityContext;
import cn.sliew.carp.framework.common.security.OnlineUserInfo;
import cn.sliew.carp.framework.web.util.RequestParamUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Slf4j
public class AsyncWebLogInterceptor implements AsyncHandlerInterceptor {

    private final TransmittableThreadLocal<Instant> threadState = new TransmittableThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        threadState.set(Instant.now());
        return true;
    }

    /**
     * exception catched by GlobalExceptionHandler and here can't be aware of ex
     *
     * @see cn.sliew.carp.framework.web.exception.GlobalExceptionHandler
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            logQuery(request, response, handler);
            ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
            if (responseWrapper != null) {
                responseWrapper.copyBodyToResponse();
            }
        } finally {
            threadState.remove();
        }
    }

    private void logQuery(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Duration duration = Duration.between(threadState.get(), Instant.now());
        if (!RequestParamUtil.ignorePath(request.getRequestURI()) && log.isDebugEnabled()) {
            String params = RequestParamUtil.formatRequestParams(request);
            OnlineUserInfo onlineUserInfo = CarpSecurityContext.get();
            String userName = "unknown";
            if (Objects.nonNull(onlineUserInfo)) {
                userName = onlineUserInfo.getUserName();
            }
            String module = "unknown";
            String desc = "unknow";
            if (Objects.nonNull(handler) && handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                Pair<String, String> pair = RequestParamUtil.findModuleAndDesc(handlerMethod);
                module = pair.getLeft();
                desc = pair.getRight();
            }
            log.debug("{} {} {} {} {} {} {}", userName, module, desc,
                    DurationFormatUtils.formatDurationHMS(duration.toMillis()),
                    request.getMethod(), request.getRequestURI(), params);
        }
    }
}
