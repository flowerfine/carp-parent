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
package cn.sliew.carp.framework.web.response;

import cn.sliew.carp.framework.common.model.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RequestWithResultMethodProcessor implements HandlerMethodReturnValueHandler, InitializingBean {

    @Autowired
    private RequestMappingHandlerAdapter adapter;

    private RequestResponseBodyMethodProcessor processor;

    @Override
    public void afterPropertiesSet() {
        List<HandlerMethodReturnValueHandler> unmodifiableList = adapter.getReturnValueHandlers();
        List<HandlerMethodReturnValueHandler> list = new ArrayList<>(unmodifiableList.size());
        for (HandlerMethodReturnValueHandler returnValueHandler : unmodifiableList) {
            if (returnValueHandler instanceof RequestResponseBodyMethodProcessor) {
                this.processor = (RequestResponseBodyMethodProcessor) returnValueHandler;
                list.add(this);
            } else {
                list.add(returnValueHandler);
            }
        }
        adapter.setReturnValueHandlers(list);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest)
            throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {
        if (supportWithResult(returnType)) {
            ResponseVO response = convertValue(returnValue);
            processor.handleReturnValue(response, returnType, mavContainer, webRequest);
        } else {
            processor.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
        }
    }

    private ResponseVO convertValue(Object returnValue) {
        return ResponseVO.success(returnValue);
    }

    private boolean supportWithResult(MethodParameter returnType) {
        if (returnType.getMethod().getReturnType().equals(ResponseVO.class)) {
            return false;
        }
        return AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), ApiResponseWrapper.class)
                || returnType.hasMethodAnnotation(ApiResponseWrapper.class);
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return processor.supportsReturnType(returnType);
    }
}