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
package cn.sliew.carp.framework.web.exception;

import cn.sliew.carp.framework.exception.ExceptionVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class ExceptionHandlerFactory {

    @Autowired
    private List<WebExceptionHandler> exceptionHandlers;

    public Optional<ExceptionVO> handle(Throwable e, HttpServletRequest request, HttpServletResponse response) {
        if (CollectionUtils.isEmpty(exceptionHandlers)) {
            return Optional.empty();
        }
        return exceptionHandlers.stream()
                .filter(handler -> handler.support(e))
                .findFirst()
                .map(handler -> handler.handle(request.getMethod() + " " + request.getRequestURI(), e, request, response));
    }
}
