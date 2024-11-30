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
package cn.sliew.carp.framework.web.util;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.sliew.carp.framework.common.model.ResponseVO;
import cn.sliew.milky.common.util.JacksonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WebUtil extends WebUtils {

    /**
     * 将字符串渲染到客户端 * * @param response 渲染对象 * @param string 待渲染的字符串 * @return null
     */
    public static void renderJson(HttpServletResponse response, ResponseVO responseVO) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        JakartaServletUtil.write(response, JacksonUtil.toJsonString(responseVO), MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * copied from hutool for javax -> jakarta
     */
    public static String getClientIP(HttpServletRequest request, String... otherHeaderNames) {
        return JakartaServletUtil.getClientIP(request, otherHeaderNames);
    }

    public static String getClientIPByHeader(HttpServletRequest request, String... headerNames) {
        return JakartaServletUtil.getClientIPByHeader(request, headerNames);
    }
}
