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
package cn.sliew.carp.framework.web.config;

import cn.sliew.carp.framework.web.converter.JacksonEnumConverter;
import cn.sliew.carp.framework.web.interceptor.AsyncWebLogInterceptor;
import cn.sliew.carp.framework.web.util.RequestParamUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.CacheControl;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private ObjectMapper mapper;

    /**
     * with purpose for supporting enum on springmvc pathvariable, jackson, mybatis-plus
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        WebMvcConfigurer.super.addFormatters(registry);
        // 一样的思路，前者利用了 jackson @JsonCreator，后者使用了枚举
        registry.addConverter(new JacksonEnumConverter(mapper));
//        registry.addConverterFactory(new DictConverterFactory());
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.getDefault());
        return localeResolver;
    }

    /**
     * 通用拦截器排除swagger设置，所有拦截器都会自动加swagger相关的资源排除信息
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        registry.addInterceptor(localeChangeInterceptor);
        WebMvcConfigurer.super.addInterceptors(registry);

        try {
            registry.addInterceptor(new AsyncWebLogInterceptor());
            List<Field> allFieldsList = FieldUtils.getAllFieldsList(InterceptorRegistry.class);
            for (Field field : allFieldsList) {
                if (field.getName().equals("registrations")) {
                    ReflectionUtils.makeAccessible(field);
                    List<InterceptorRegistration> registrations = (List<InterceptorRegistration>) ReflectionUtils.getField(field, registry);
                    if (registrations != null) {
                        for (InterceptorRegistration interceptorRegistration : registrations) {
                            interceptorRegistration.excludePathPatterns(RequestParamUtil.getIgnorePaths());
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/")
                .setCacheControl(CacheControl.maxAge(5, TimeUnit.HOURS).cachePublic());
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/ui/");
        registry.addViewController("/ui/").setViewName("forward:/ui/index.html");
    }

}
