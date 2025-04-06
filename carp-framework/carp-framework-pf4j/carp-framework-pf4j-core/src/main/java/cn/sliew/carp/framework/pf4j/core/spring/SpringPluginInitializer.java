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
package cn.sliew.carp.framework.pf4j.core.spring;

import cn.sliew.carp.framework.pf4j.core.config.ConfigFactory;
import cn.sliew.carp.framework.pf4j.core.events.ApplicationEventListenerBeanPostProcessor;
import cn.sliew.carp.framework.pf4j.core.spring.context.ComponentScanningCustomizer;
import cn.sliew.carp.framework.pf4j.core.spring.context.PluginConfigurationRegisteringCustomizer;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.Arrays;

/**
 * Initializes the given {@link Plugin}'s {@link GenericApplicationContext} after being connected to the service's
 * own {@link ApplicationContext}.
 */
@Slf4j
public class SpringPluginInitializer implements ApplicationContextAware {

    private final Plugin plugin;
    private final PluginWrapper pluginWrapper;
    private final GenericApplicationContext pluginApplicationContext;

    public SpringPluginInitializer(
            Plugin plugin,
            PluginWrapper pluginWrapper,
            GenericApplicationContext pluginApplicationContext) {
        this.plugin = plugin;
        this.pluginWrapper = pluginWrapper;
        this.pluginApplicationContext = pluginApplicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        if (!(applicationContext instanceof ConfigurableApplicationContext)) {
            throw new RuntimeException("ApplicationContext must be configurable");
        }

        log.info("Initializing '{}'", pluginWrapper.getPluginId());
        pluginApplicationContext.setClassLoader(pluginWrapper.getPluginClassLoader());
        pluginApplicationContext.setResourceLoader(new DefaultResourceLoader(pluginWrapper.getPluginClassLoader()));

        pluginApplicationContext
                .getBeanFactory()
                .addBeanPostProcessor(new ApplicationEventListenerBeanPostProcessor());

        Arrays.asList(
                new PluginConfigurationRegisteringCustomizer(applicationContext.getBean(ConfigFactory.class)),
                new ComponentScanningCustomizer()
        ).forEach(customizer ->
                customizer.accept(plugin, pluginApplicationContext)
        );

        pluginApplicationContext.refresh();
    }
}
