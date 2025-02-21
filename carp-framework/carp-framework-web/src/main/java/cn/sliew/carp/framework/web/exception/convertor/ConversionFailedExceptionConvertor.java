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
import cn.sliew.carp.framework.exception.ExceptionVO;
import cn.sliew.carp.framework.web.exception.WebExceptionHandler;
import cn.sliew.carp.framework.web.util.RequestParamUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(ConversionFailedExceptionConvertor.ORDER)
public class ConversionFailedExceptionConvertor implements WebExceptionHandler<ConversionFailedException> {

    static final int ORDER = SliewExceptionConvertor.ORDER - 3;

    @Override
    public boolean support(ConversionFailedException e) {
        return ConversionFailedException.class.equals(e.getClass());
    }

    @Override
    public ExceptionVO handle(String name, ConversionFailedException e, HttpServletRequest request, HttpServletResponse response) {
        String params = RequestParamUtil.formatRequestParams(request);
        log.error("{} {} {}", request.getMethod(), request.getRequestURI(), params, e);
        final TypeDescriptor sourceType = e.getSourceType();
        final TypeDescriptor targetType = e.getTargetType();
        final Object value = e.getValue();
        return new ExceptionVO(
                ResponseCodeEnum.ERROR.getCode(),
                String.format("springmvc convert %s from %s to %s error",
                        value, sourceType.getName(), targetType.getName()),
                null,
                false);
    }

}
