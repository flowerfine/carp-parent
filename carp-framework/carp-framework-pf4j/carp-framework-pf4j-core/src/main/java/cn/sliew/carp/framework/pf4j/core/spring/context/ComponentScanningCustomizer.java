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
package cn.sliew.carp.framework.pf4j.core.spring.context;

import cn.sliew.carp.framework.pf4j.api.PluginComponent;
import cn.sliew.carp.framework.pf4j.api.internal.CarpExtensionPoint;
import cn.sliew.carp.framework.pf4j.core.spring.PluginUtils;
import org.pf4j.Plugin;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

/**
 * Scans a {@link Plugin} classpath for known annotated classes that should be registered into the bean factory.
 */
public class ComponentScanningCustomizer implements PluginApplicationContextCustomizer {

    @Override
    public void accept(Plugin plugin, ConfigurableApplicationContext context) {
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(
                (GenericApplicationContext) context,
                false,
                context.getEnvironment()
        );

        scanner.addIncludeFilter(new AnnotationTypeFilter(PluginComponent.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(CarpExtensionPoint.class));

        scanner.scan(PluginUtils.getBasePackageName(plugin));
    }
}
