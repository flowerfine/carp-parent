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
package cn.sliew.carp.framework.spring.jackson;

import cn.sliew.carp.framework.common.jackson.subtype.JsonTypeNameParser;
import cn.sliew.carp.framework.common.jackson.subtype.NamedTypeParser;
import cn.sliew.carp.framework.common.jackson.subtype.SubtypeLocator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import jakarta.annotation.Nonnull;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.List;
import java.util.Objects;

/**
 * Handles discovery and registration of ObjectMapper subtypes.
 *
 * <p>Using the NAME JsonTypeInfo strategy, each subtype needs to be defined explicitly. If all
 * subtypes are known at compile time, the subtypes should be defined on the root type. However,
 * Spinnaker often times offers addition of new types, which may require scanning different packages
 * for additional subtypes. This class will assist this specific task.
 */
public class ObjectMapperSubtypeConfigurer {

    private final NamedTypeParser namedTypeParser;

    public ObjectMapperSubtypeConfigurer(boolean strictSerialization) {
        namedTypeParser = new JsonTypeNameParser(strictSerialization);
    }

    /**
     * Constructs this with a custom {@link NamedTypeParser} to extract JSON type discriminator info.
     * This is useful for supporting custom annotations for type discriminators.
     */
    public ObjectMapperSubtypeConfigurer(@Nonnull NamedTypeParser namedTypeParser) {
        this.namedTypeParser = namedTypeParser;
    }

    public void registerSubtypes(ObjectMapper mapper, List<SubtypeLocator> subtypeLocators) {
        subtypeLocators.forEach(locator -> registerSubtype(mapper, locator));
    }

    public void registerSubtype(ObjectMapper mapper, SubtypeLocator subtypeLocator) {
        subtypeLocator
                .searchPackages()
                .forEach(pkg -> mapper.registerSubtypes(findSubtypes(subtypeLocator.rootType(), pkg)));
    }

    private NamedType[] findSubtypes(Class<?> clazz, String pkg) {
        ClassPathScanningCandidateComponentProvider provider =
                new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(clazz));

        return provider.findCandidateComponents(pkg).stream()
                .map(
                        bean -> {
                            String beanClassName = bean.getBeanClassName();
                            if (beanClassName == null) {
                                return null;
                            }
                            Class<?> type =
                                    ClassUtils.resolveClassName(beanClassName, ClassUtils.getDefaultClassLoader());
                            return namedTypeParser.parse(type);
                        })
                .filter(Objects::nonNull)
                .toArray(NamedType[]::new);
    }

}
