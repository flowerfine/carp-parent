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

import cn.sliew.carp.framework.common.model.ResponseVO;
import cn.sliew.carp.framework.exception.SliewException;
import cn.sliew.carp.framework.web.exception.convertor.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * @see DefaultHandlerExceptionResolver
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * All exception handling converters
     */
    public static final Map<Class<?>, ExceptionConvertor> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put(Throwable.class, new ThrowableConvertor());
        REGISTRY.put(Exception.class, new CommonExceptionConvertor());
        REGISTRY.put(SliewException.class, new SliewExceptionConvertor());

        REGISTRY.put(ConversionFailedException.class, new ConversionFailedExceptionConvertor());
        REGISTRY.put(BindException.class, new BindExceptionConvertor());
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ResponseVO> exception(Throwable exception,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
        ResponseVO errorInfo = convert(exception, request, response);
        return new ResponseEntity<>(errorInfo, HttpStatus.OK);
    }

    public ResponseVO convert(Throwable exception, HttpServletRequest request, HttpServletResponse response) {
        ExceptionConvertor exceptionConvertor = REGISTRY.get(exception.getClass());
        if (exceptionConvertor == null) {
            if (exception instanceof SliewException) {
                exceptionConvertor = REGISTRY.get(SliewException.class);
            } else if (exception instanceof Exception) {
                exceptionConvertor = REGISTRY.get(Exception.class);
            } else {
                exceptionConvertor = REGISTRY.get(Throwable.class);
            }
        }
        return exceptionConvertor.convert(exception, request, response);
    }
}
