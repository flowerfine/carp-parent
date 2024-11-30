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
import org.apache.commons.lang3.reflect.TypeUtils;
import org.springdoc.core.parsers.ReturnTypeParser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
@ConditionalOnClass(ReturnTypeParser.class)
public class Knife4jApiResponseHandler implements ReturnTypeParser {

    @Override
    public Type getReturnType(MethodParameter methodParameter) {
        Type returnType = ReturnTypeParser.super.getReturnType(methodParameter);
        if (methodParameter.getMethod().getReturnType().equals(ResponseVO.class)) {
            return returnType;
        }

        if (AnnotatedElementUtils.hasAnnotation(methodParameter.getContainingClass(), ApiResponseWrapper.class)
                || methodParameter.hasMethodAnnotation(ApiResponseWrapper.class)) {
            if (returnType == void.class || returnType == Void.class) {
                return TypeUtils.parameterize(ResponseVO.class, Void.class);
            }
            return TypeUtils.parameterize(ResponseVO.class, returnType);
        }

        return returnType;
    }
}
