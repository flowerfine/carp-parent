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
package cn.sliew.carp.framework.web.exception.convertor;

import cn.sliew.carp.framework.common.enums.ResponseCodeEnum;
import cn.sliew.carp.framework.common.exception.SliewException;
import cn.sliew.carp.framework.exception.ExceptionVO;
import cn.sliew.carp.framework.web.exception.WebExceptionHandler;
import cn.sliew.carp.framework.web.util.RequestParamUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@Order(SliewExceptionConvertor.ORDER)
public class SliewExceptionConvertor implements WebExceptionHandler {

    public static final int ORDER = ExceptionConvertor.ORDER - 1;

    @Override
    public boolean support(Throwable e) {
        return e.getClass().isAssignableFrom(SliewException.class);
    }

    @Override
    public ExceptionVO handle(String name, Throwable e, HttpServletRequest request, HttpServletResponse response) {
        String params = RequestParamUtil.formatRequestParams(request);
        log.error("{} {} {}", request.getMethod(), request.getRequestURI(), params, e);
        if (e instanceof SliewException exception) {
            if (StringUtils.hasText(exception.getCode())) {
                return new ExceptionVO(exception.getCode(), exception.getMessage(), null, exception.getRetryable());
            } else {
                return new ExceptionVO(ResponseCodeEnum.ERROR.getCode(), exception.getMessage(), null, exception.getRetryable());
            }
        }
        return null;
    }
}
