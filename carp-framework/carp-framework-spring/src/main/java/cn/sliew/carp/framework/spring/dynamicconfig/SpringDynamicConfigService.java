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
package cn.sliew.carp.framework.spring.dynamicconfig;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import javax.annotation.Nonnull;

import static java.lang.String.format;

/**
 * The SpringDynamicConfigService directly interacts with the Spring Environment.
 */
public class SpringDynamicConfigService implements DynamicConfigService, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public <T> T getConfig(
            @Nonnull Class<T> configType, @Nonnull String configName, @Nonnull T defaultValue) {
        if (environment == null) {
            return defaultValue;
        }
        return environment.getProperty(configName, configType, defaultValue);
    }

    @Override
    public boolean isEnabled(@Nonnull String flagName, boolean defaultValue) {
        if (environment == null) {
            return defaultValue;
        }
        return environment.getProperty(flagPropertyName(flagName), Boolean.class, defaultValue);
    }

    private static String flagPropertyName(String flagName) {
        return flagName.endsWith(".enabled") ? flagName : format("%s.enabled", flagName);
    }
}
