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

import cn.hutool.extra.spring.SpringUtil;
import cn.sliew.carp.framework.log.web.annotation.WebLog;
import cn.sliew.carp.framework.log.web.enums.LogEntity;
import cn.sliew.milky.common.util.JacksonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class RequestParamUtil {

    private static String contextPath;

    private static final String IGNORE_CONTENT_TYPE = "multipart/form-data";
    private static List<String> DEFAULT_IGNORE_PATH = Arrays.asList(
            "/doc.html", "/swagger-resources", "/webjars/**", "/v3/api-docs/**", "/favicon.ico", "/ui/**/**");
    private static List<String> IGNORE_PATH = Collections.emptyList();
    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    public static String formatRequestParams(HttpServletRequest request) {
        StringBuilder params = new StringBuilder();
        if (!(RequestParamUtil.ignoreContentType(request.getContentType()))) {
            String parameters = RequestParamUtil.getRequestParams(request);
            if (StringUtils.hasText(parameters)) {
                params.append("uri_params: [");
                params.append(parameters);
                params.append("]");
            }
            String body = RequestParamUtil.getRequestBody(request);
            if (StringUtils.hasText(body)) {
                if (params.length() != 0) {
                    params.append(", request_body: [");
                } else {
                    params.append("request_body: [");
                }
                params.append(body);
                params.append("]");
            }
        }

        return params.toString();
    }

    /**
     * query param
     */
    public static String getRequestParams(HttpServletRequest request) {
        if (request.getParameterMap().isEmpty()) {
            return "";
        }

        Map<String, Object> query = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            final String key = entry.getKey();
            final String[] value = entry.getValue();

            if (value == null || value.length == 0) {
                query.put(key, null);
            } else if (value.length == 1) {
                query.put(key, value[0]);
            } else {
                query.put(key, value);
            }
        }
        return JacksonUtil.toJsonString(query);
    }

    /**
     * body param
     */
    public static String getRequestBody(HttpServletRequest request) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                String payload;
                try {
                    payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException e) {
                    payload = "[unknown]";
                }
                return payload.replaceAll("\\n", "");
            }
        }
        return "";
    }

    public static boolean isRequestValid(HttpServletRequest request) {
        try {
            new URI(request.getRequestURL().toString());
            return true;
        } catch (URISyntaxException ex) {
            return false;
        }
    }

    public static boolean ignorePath(String uri) {
        return getIgnorePaths().stream()
                .filter(pattern -> ANT_PATH_MATCHER.match(pattern, uri))
                .findAny()
                .isPresent();
    }

    public static boolean ignoreContentType(String contentType) {
        return StringUtils.hasText(contentType) && contentType.startsWith(IGNORE_CONTENT_TYPE);
    }

    public static List<String> getIgnorePaths() {
        if (CollectionUtils.isEmpty(IGNORE_PATH) && StringUtils.hasText(getContextPath())) {
            IGNORE_PATH = getDefaultIgnorePaths().stream().map(path -> "/" + getContextPath() + path).collect(Collectors.toList());
        }
        return IGNORE_PATH;
    }

    public static List<String> getDefaultIgnorePaths() {
        return DEFAULT_IGNORE_PATH;
    }

    public static String getContextPath() {
        if (StringUtils.hasText(contextPath)) {
            return contextPath;
        }
        contextPath = SpringUtil.getProperty("server.servlet.context-path");
        return contextPath;
    }

    public static HandlerMethod getHandlerMethod() {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        if (Objects.nonNull(requestAttributes)) {
            if (requestAttributes instanceof ServletRequestAttributes) {
                ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
                HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
                Optional<Object> optional = Optional.ofNullable(httpServletRequest).map(object -> {
                    try {
                        RequestMappingInfoHandlerMapping handlerMapping = SpringUtil.getBean("requestMappingHandlerMapping", RequestMappingInfoHandlerMapping.class);
                        return handlerMapping.getHandler(httpServletRequest);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).map(chain -> chain.getHandler());
                if (optional.isPresent()) {
                    Object handler = optional.get();
                    if (handler instanceof HandlerMethod) {
                        return (HandlerMethod) handler;
                    }
                }
            }
        }
        return null;
    }

    public static Set<LogEntity> findLogEntry(HandlerMethod handlerMethod) {
        WebLog methodWebLog = handlerMethod.getMethodAnnotation(WebLog.class);
        WebLog classWebLog = handlerMethod.getBeanType().getDeclaredAnnotation(WebLog.class);
        return LogEntity.getIncludes(methodWebLog, classWebLog);
    }

    public static Pair<String, String> findModuleAndDesc(HandlerMethod handlerMethod) {
        WebLog methodWebLog = handlerMethod.getMethodAnnotation(WebLog.class);
        WebLog classWebLog = handlerMethod.getBeanType().getDeclaredAnnotation(WebLog.class);
        String module = null;
        String desc = null;
        if (Objects.nonNull(methodWebLog)) {
            module = methodWebLog.module();
            desc = methodWebLog.desc();
        }
        if (Objects.nonNull(classWebLog)) {
            if (StringUtils.hasText(module) == false) {
                module = classWebLog.module();
            }
            if (StringUtils.hasText(desc) == false) {
                desc = classWebLog.desc();
            }
        }

        // 使用 swagger 注解补偿
        if (StringUtils.hasText(module) == false) {
            Tag tag = handlerMethod.getBeanType().getDeclaredAnnotation(Tag.class);
            module = Optional.ofNullable(tag).map(Tag::name).orElse(null);
        }
        if (StringUtils.hasText(desc) == false) {
            Operation operation = handlerMethod.getMethodAnnotation(Operation.class);
            if (Objects.nonNull(operation)) {
                desc = StringUtils.hasText(operation.summary()) ? operation.summary() : operation.description();
            }
        }
        return Pair.of(module, desc);
    }
}