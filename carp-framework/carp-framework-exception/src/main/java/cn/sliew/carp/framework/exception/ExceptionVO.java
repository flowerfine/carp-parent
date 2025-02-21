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

import com.google.common.base.Strings;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class ExceptionVO {

    private final String errorCode;
    private final String errorMessage;
    private final Map<String, Object> details;
    private final Date timestamp = new Date();
    private boolean retryable;

    public ExceptionVO(String errorCode, String errorMessage, Map<String, Object> details, boolean retryable) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.details = details == null ? new HashMap<>() : new HashMap<>(details);
        this.retryable = retryable;
    }

    static Map<String, Object> buildDetails(String error) {
        return buildDetails(error, null);
    }

    static Map<String, Object> buildDetails(String error, List<String> errors) {
        Map<String, Object> details = new HashMap<>();
        details.put("error", error);
        if (errors == null) {
            errors = Collections.emptyList();
        }

        details.put(
                "errors",
                errors.stream().filter(x -> !Strings.isNullOrEmpty(x)).collect(Collectors.toList()));
        return details;
    }
}
