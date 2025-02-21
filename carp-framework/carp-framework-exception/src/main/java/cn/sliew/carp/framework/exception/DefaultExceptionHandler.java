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
package cn.sliew.carp.framework.exception;

import com.google.common.base.Throwables;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
@Order(DefaultExceptionHandler.ORDER)
public class DefaultExceptionHandler implements ExceptionHandler<Throwable> {

    public static final int ORDER = Ordered.LOWEST_PRECEDENCE;

    @Override
    public boolean support(Throwable e) {
        return true;
    }

    @Override
    public ExceptionVO handle(String name, Throwable e) {
        Map<String, Object> exceptionDetails =
                ExceptionVO.buildDetails("Unexpected Failure", Collections.singletonList(e.getMessage()));
        exceptionDetails.put("name", name);
        exceptionDetails.put("exceptionType", e.getClass().getSimpleName());
        exceptionDetails.put("stackTrace", Throwables.getStackTraceAsString(e));
        if (e instanceof SliewException sliewException) {
            exceptionDetails.putAll(sliewException.getAdditionalAttributes());
        }
        return new ExceptionVO(
                "-1", "unknown exception", exceptionDetails, false);
    }
}
