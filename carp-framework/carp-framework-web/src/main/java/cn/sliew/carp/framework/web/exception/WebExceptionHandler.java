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
import cn.sliew.carp.framework.exception.ExceptionHandler;
import cn.sliew.carp.framework.exception.ExceptionVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface WebExceptionHandler<T extends Throwable> extends ExceptionHandler<T> {

    @Override
    default ExceptionVO handle(String name, T e) {
        return new ExceptionVO(
                ResponseCodeEnum.ERROR.getCode(),
                ResponseCodeEnum.ERROR.getValue(),
                null,
                false);
    }

    default ExceptionVO handle(String name, T e, HttpServletRequest request, HttpServletResponse response) {
        return handle(name, e);
    }

}
