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

import lombok.Getter;
import org.pf4j.Plugin;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * A container class for the actual plugin.
 * <p>
 * This exists so we can perform our own logic on the "plugin" without exposing internal concerns
 * to the plugin developer. This container does, however, mean that plugin framework developers
 * must be sure _not_ to use this class in {@code plugin.getClass()} operations, instead using
 * {@code plugin.getActual().getClass()} to get access to the actual plugin Java class.
 */
public class PluginContainer extends Plugin {

    /**
     * The actual plugin-provided {@link Plugin} class.
     */
    @Getter
    private final Plugin actual;
    /**
     * plugin 会 start、stop、delete 等，kork 并没有联动调用 pluginContext 相关操作。
     */
    final GenericApplicationContext pluginContext;

    public PluginContainer(
            Plugin actual,
            GenericApplicationContext serviceApplicationContext) {
        super(actual.getWrapper());
        this.actual = actual;
        this.pluginContext = new GenericApplicationContext(serviceApplicationContext);
        ApplicationContextGraph.pluginContexts.put(getWrapper().getPluginId(), this.pluginContext);
    }

    /**
     * Registers the {@link SpringPluginInitializer} with the service {@link ApplicationContext} so that the plugin can be
     * initialized at the correct time in the service's startup process.
     */
    public String registerInitializer(BeanDefinitionRegistry registry) {
        String initializerBeanName = getWrapper().getPluginId() + "Initializer";

        BeanDefinition initializerBeanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(SpringPluginInitializer.class)
                .setScope(BeanDefinition.SCOPE_SINGLETON)
                .setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_NO)
                .addConstructorArgValue(actual)
                .addConstructorArgValue(actual.getWrapper())
                .addConstructorArgValue(pluginContext)
                .getBeanDefinition();

        registry.registerBeanDefinition(initializerBeanName, initializerBeanDefinition);
        return initializerBeanName;
    }

    @Override
    public void start() {
        actual.start();
//        pluginContext.start();
    }

    @Override
    public void stop() {
        actual.stop();
//        pluginContext.stop();
    }

    @Override
    public void delete() {
        actual.delete();
//        ApplicationContextGraph.pluginContexts.remove(getWrapper().getPluginId());
    }
}
