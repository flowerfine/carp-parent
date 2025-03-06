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
package cn.sliew.carp.framework.common.util;

import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import static com.google.common.base.Preconditions.checkArgument;

public enum CacheUtil {
    ;

    private static final String SEPARATOR = ":";

    public static String buildCacheKey(@NotBlank String tag, Object... obj) {
        checkArgument(StringUtils.isNotBlank(tag), "tag must not blank");
        String.join(SEPARATOR);
        StringBuilder key = new StringBuilder(tag);
        if (ArrayUtils.isNotEmpty(obj)) {
            for (int i = 0; i < obj.length; i++) {
                if (i == obj.length - 1) {
                    key.append(obj[i]);
                } else {
                    key.append(obj[i]).append(SEPARATOR);
                }
            }
        }
        return key.toString();
    }

}
