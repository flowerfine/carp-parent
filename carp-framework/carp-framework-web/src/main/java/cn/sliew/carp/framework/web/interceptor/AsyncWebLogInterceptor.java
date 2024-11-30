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

import cn.sliew.carp.framework.web.util.RequestParamUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

@Slf4j
public class AsyncWebLogInterceptor implements AsyncHandlerInterceptor {

    /**
     * exception catched by GlobalExceptionHandler and here can't be aware of ex
     *
     * @see cn.sliew.carp.framework.web.exception.GlobalExceptionHandler
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logQuery(request);
        ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (responseWrapper != null) {
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logQuery(HttpServletRequest request) {
        if (!RequestParamUtil.ignorePath(request.getRequestURI()) && log.isInfoEnabled()) {
            String params = RequestParamUtil.formatRequestParams(request);
            log.info("{} {} {}", request.getMethod(), request.getRequestURI(), params);
        }
    }
}
