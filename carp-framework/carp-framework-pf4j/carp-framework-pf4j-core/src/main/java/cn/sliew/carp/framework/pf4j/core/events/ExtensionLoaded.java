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
package cn.sliew.carp.framework.pf4j.core.events;

import cn.sliew.carp.framework.pf4j.core.pf4j.finders.UnsafePluginDescriptor;
import cn.sliew.carp.framework.pf4j.core.spring.ExtensionBeanDefinitionRegistryPostProcessor;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ExtensionLoaded extends ApplicationEvent {

    private final String beanName;
    private final Class<?> beanClass;
    private final UnsafePluginDescriptor pluginDescriptor;

    /**
     * A Spring {@link ApplicationEvent} that is emitted when an extension is loaded.
     *
     * @param source           The source of the event
     * @param beanName         The name of the extension Spring bean for the extension
     * @param beanClass        The extension bean type
     * @param pluginDescriptor The plugin descriptor, if the extension was provided by a plugin
     */
    public ExtensionLoaded(
            ExtensionBeanDefinitionRegistryPostProcessor source,
            String beanName,
            Class<?> beanClass,
            UnsafePluginDescriptor pluginDescriptor) {
        super(source);
        this.beanName = beanName;
        this.beanClass = beanClass;
        this.pluginDescriptor = pluginDescriptor;
    }

    public ExtensionLoaded(
            ExtensionBeanDefinitionRegistryPostProcessor source,
            String beanName,
            Class<?> beanClass) {
        this(source, beanName, beanClass, null);
    }
}
