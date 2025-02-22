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

import cn.sliew.carp.framework.common.enums.ResponseCodeEnum;
import cn.sliew.carp.framework.common.model.ResponseVO;
import cn.sliew.carp.framework.exception.ExceptionVO;
import cn.sliew.carp.framework.exception.SliewException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import java.util.Optional;

/**
 * @see DefaultHandlerExceptionResolver
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private ExceptionHandlerFactory exceptionHandlerFactory;

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ResponseVO> handleThrowalbe(Throwable exception,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) {
        ResponseVO errorInfo = convert(exception, request, response);
        return new ResponseEntity<>(errorInfo, HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseVO> handleException(Exception exception,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) {
        ResponseVO errorInfo = convert(exception, request, response);
        return new ResponseEntity<>(errorInfo, HttpStatus.OK);
    }

    @ExceptionHandler(SliewException.class)
    public ResponseEntity<ResponseVO> handleSliewException(SliewException exception,
                                                           HttpServletRequest request,
                                                           HttpServletResponse response) {
        ResponseVO errorInfo = convert(exception, request, response);
        return new ResponseEntity<>(errorInfo, HttpStatus.OK);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseVO> handleBadRequestException(BadRequestException exception,
                                                                HttpServletRequest request,
                                                                HttpServletResponse response) {
        ResponseVO errorInfo = convert(exception, request, response);
        return new ResponseEntity<>(errorInfo, HttpStatus.OK);
    }

    @ExceptionHandler(ConversionFailedException.class)
    public ResponseEntity<ResponseVO> handleConversionFailedException(ConversionFailedException exception,
                                                                      HttpServletRequest request,
                                                                      HttpServletResponse response) {
        ResponseVO errorInfo = convert(exception, request, response);
        return new ResponseEntity<>(errorInfo, HttpStatus.OK);
    }

    public ResponseVO convert(Throwable exception, HttpServletRequest request, HttpServletResponse response) {
        Optional<ExceptionVO> optional = exceptionHandlerFactory.handle(exception, request, response);
        if (optional.isPresent()) {
            ExceptionVO exceptionVO = optional.get();
            return ResponseVO.error(exceptionVO.getErrorCode(), exceptionVO.getErrorMessage());
        }
        return ResponseVO.error(ResponseCodeEnum.ERROR.getCode(), ResponseCodeEnum.ERROR.getValue());
    }
}
