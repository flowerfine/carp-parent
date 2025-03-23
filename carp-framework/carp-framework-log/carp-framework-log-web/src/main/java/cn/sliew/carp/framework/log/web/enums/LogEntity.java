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
package cn.sliew.carp.framework.log.web.enums;

import cn.sliew.carp.framework.log.web.annotation.WebLog;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;

public enum LogEntity {

    HTTP_REQUEST_HEADERS,
    HTTP_REQUEST_BODY,
    HTTP_REQUEST_PARAM,
    HTTP_RESPONSE_BODY,
    ;

    public static Set<LogEntity> getIncludes(WebLog methodLog, WebLog classLog) {
        Set<LogEntity> classIncludes = getClassIncludes(classLog);
        return getMethodIncludes(classIncludes, methodLog);
    }

    private static Set<LogEntity> getClassIncludes(WebLog classLog) {
        if (Objects.isNull(classLog)) {
            return Collections.emptySet();
        }
        // 如果有 includes 直接返回，否则处理 excludes
        Optional<Set<LogEntity>> includeOptional = Optional.ofNullable(classLog).map(WebLog::includes)
                .filter(includes -> ArrayUtils.isNotEmpty(includes))
                .map(Set::of);
        if (includeOptional.isPresent()) {
            return includeOptional.get();
        }
        Set<LogEntity> includeSet = new HashSet<>(Arrays.asList(LogEntity.values()));
        Optional.ofNullable(classLog).map(WebLog::excludes)
                .filter(excludes -> ArrayUtils.isNotEmpty(excludes))
                .map(Set::of)
                .ifPresent(excludes -> includeSet.removeAll(excludes));
        return includeSet;
    }

    private static Set<LogEntity> getMethodIncludes(Set<LogEntity> classIncludeSet, WebLog methodLog) {
        if (Objects.isNull(methodLog)) {
            return classIncludeSet;
        }
        // 如果有 includes 合并 class 和 method 级别后返回，否则处理 excludes
        Set<LogEntity> methodIncludeSet = new HashSet<>();
        Optional<Set<LogEntity>> includeOptional = Optional.ofNullable(methodLog).map(WebLog::includes)
                .filter(includes -> ArrayUtils.isNotEmpty(includes))
                .map(Set::of);
        if (includeOptional.isPresent()) {
            methodIncludeSet.addAll(classIncludeSet);
            methodIncludeSet.addAll(includeOptional.get());
            return methodIncludeSet;
        }

        if (CollectionUtils.isEmpty(classIncludeSet)) {
            methodIncludeSet.addAll(Arrays.asList(LogEntity.values()));
        } else {
            methodIncludeSet.addAll(classIncludeSet);
        }
        Optional.ofNullable(methodLog).map(WebLog::excludes)
                .filter(excludes -> ArrayUtils.isNotEmpty(excludes))
                .map(Set::of)
                .ifPresent(excludes -> methodIncludeSet.removeAll(excludes));
        return methodIncludeSet;
    }
}
