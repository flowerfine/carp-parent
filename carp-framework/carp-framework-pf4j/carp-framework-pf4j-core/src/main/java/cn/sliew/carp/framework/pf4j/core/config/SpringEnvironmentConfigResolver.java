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
package cn.sliew.carp.framework.pf4j.core.config;

import cn.sliew.milky.common.util.JacksonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

@Slf4j
public class SpringEnvironmentConfigResolver implements ConfigResolver {

    private ConfigurableEnvironment environment;

    private ObjectMapper mapper = JacksonUtil.getMapper().copy()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    public SpringEnvironmentConfigResolver(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public <T> T resolve(ConfigCoordinates coordinates, Class<T> expectedType) {
        return resolveInternal(
                coordinates,
                () -> mapper.convertValue(Collections.emptyMap(), expectedType),
                parser -> {
                    try {
                        return mapper.readValue(parser, expectedType);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public <T> T resolve(ConfigCoordinates coordinates, TypeReference<T> expectedType) {
        return resolveInternal(
                coordinates,
                () -> {
                    // Yo, it's fine. Totally fine.
                    @SuppressWarnings("unchecked")
                    Class<T> type = (Class<T>) ((ParameterizedType) expectedType.getType()).getRawType();
                    if (type.isInterface()) {
                        // TODO(rz): Maybe this should be supported, but there's only one use at this point, and we can just call in
                        //  with HashMap instead of Map.
                        throw new SystemConfigException("Expected type must be a concrete class, interface given");
                    }
                    return mapper.convertValue(Collections.emptyMap(), type);
                },
                parser -> {
                    try {
                        return mapper.readValue(parser, expectedType);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private <T> T resolveInternal(ConfigCoordinates coordinates,
                                  Supplier<T> missingCallback,
                                  Function<TreeTraversingParser, T> callback) {
        String pointer = coordinates.toPointer();
        log.debug("Searching for config at '{}'", pointer);
        JsonNode tree = mapper.valueToTree(propertySourcesAsMap()).at(pointer);
        if (tree instanceof MissingNode) {
            log.debug("Missing configuration for '{}': Loading default", coordinates);
            return missingCallback.get();
        }

        log.debug("Found config at '{}'", pointer);

        try {
            return callback.apply(new TreeTraversingParser(tree, mapper));
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed resolving extension config for an unexpected reason", e);
        }
    }

    private Map<String, Object> propertySourcesAsMap() {
        Map<String, Object> result = new HashMap<>();

        StreamSupport.stream(environment.getPropertySources().spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> (EnumerablePropertySource<?>) ps)
                .toList()
                .forEach(ps -> result.putAll(toRelevantProperties(ps)));

        Properties properties = new Properties();
        properties.putAll(result);

        try {
            return new JavaPropsMapper().readPropertiesAs(properties, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert properties to map", e);
        }
    }

    /**
     * Filters out the configs that we don't care about and converts the config properties into a Map
     */
    private Map<String, Object> toRelevantProperties(EnumerablePropertySource<?> propertySource) {
        Map<String, Object> result = new HashMap<>();
        for (String propertyName : propertySource.getPropertyNames()) {
            if (propertyName.startsWith(ConfigCoordinates.CONFIG_NAMESPACE)) {
                Object value = propertySource.getProperty(propertyName);
                if (!(value instanceof Map) || !((Map<?, ?>) value).isEmpty()) {
                    result.put(propertyName, value);
                }
            }
        }
        return result;
    }


    private class SystemConfigException extends RuntimeException {
        public SystemConfigException(String message) {
            super(message);
        }
    }
}
