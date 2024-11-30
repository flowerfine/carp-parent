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
package cn.sliew.carp.framework.common.constant;

import cn.hutool.core.date.DatePattern;

import java.io.File;

public enum Constants {
    ;

    /**
     * 默认日期格式
     */
    public static final String DEFAULT_DATE_FORMAT = DatePattern.NORM_DATE_PATTERN;
    public static final String DEFAULT_DATETIME_FORMAT = DatePattern.NORM_DATETIME_PATTERN;
    public static final String MS_DATETIME_FORMAT = DatePattern.NORM_DATETIME_MS_PATTERN;

    public static final String SEPARATOR = "-";
    public static final String PATH_SEPARATOR = File.separator;

    public static final String DEFAULT_TIMEZONE = "GMT+8";
    public static final String DEFAULT_CHARSET = "UTF-8";

    public static final String CODEC_STR_PREFIX = "Encrypted:";

    public static final String SORT_ASC = "asc";
    // antd 升序枚举
    public static final String SORT_ASC_ANTD = "ascend";
    public static final String SORT_DESC = "desc";
    // antd 降序枚举
    public static final String SORT_DESC_ANTD = "descend";
}
