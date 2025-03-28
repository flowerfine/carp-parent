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
package cn.sliew.carp.framework.queue.kekio.configuration;

import cn.sliew.milky.common.util.JacksonUtil;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@EnableConfigurationProperties(KekioObjectMapperSubtypeProperties.class)
public class KekioObjectMapperConfiguration {

    static final String KEKIO_OBJECT_MAPPER = "cn.sliew.carp.framework.queue.kekio.configuration.KekioObjectMapper";

    @Autowired
    private KekioObjectMapperSubtypeProperties properties;

    @Bean(name = KEKIO_OBJECT_MAPPER)
    public ObjectMapper kekioObjectMapper() {
        ObjectMapper mapper = JacksonUtil.getMapper().copy();
        registerSubtypes(mapper, properties.getMessageRootType(), properties.getMessagePackages());
        registerSubtypes(mapper, properties.getAttributeRootType(), properties.getAttributePackages());

        properties.getExtraSubtypes().forEach((rootType, subtypePackages) -> {
            registerSubtypes(mapper, rootType, subtypePackages);
        });
        return mapper;
    }

    private void registerSubtypes(ObjectMapper mapper, String rootType, List<String> subtypePackages) {
        Class rootTypeClass = getRootTypeClass(rootType);
        subtypePackages.forEach(it -> mapper.registerSubtypes(findSubtypes(rootTypeClass, it)));
    }

    private NamedType[] findSubtypes(Class rootTypeClass, String packageName) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(rootTypeClass));

        Set<BeanDefinition> candidateComponents = provider.findCandidateComponents(packageName);
        return candidateComponents.stream().map(it -> {
            if (StringUtils.isNotBlank(it.getBeanClassName())) {
                Class<?> clazz = ClassUtils.resolveClassName(it.getBeanClassName(), ClassUtils.getDefaultClassLoader());
                JsonTypeName annotation = clazz.getAnnotation(JsonTypeName.class);
                if (Objects.nonNull(annotation)) {
                    if (StringUtils.isBlank(annotation.value())) {
                        throw new RuntimeException(
                                String.format("Subtype %s does not have a JsonTypeName", clazz.getSimpleName()));
                    }
                    log.info("Registering subtype of {}: {} to KekioObjectMapper", clazz.getSimpleName(), annotation.value());
                    return new NamedType(clazz, annotation.value());
                }
            }
            return null;
        }).filter(it -> Objects.nonNull(it)).toArray(NamedType[]::new);
    }

    private Class getRootTypeClass(String name) {
        return ClassUtils.resolveClassName(name, ClassUtils.getDefaultClassLoader());
    }

}
