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

import cn.sliew.carp.framework.pf4j.api.PluginConfiguration;
import cn.sliew.carp.framework.pf4j.core.config.ConfigFactory;
import cn.sliew.carp.framework.pf4j.core.spring.PluginUtils;
import org.pf4j.Plugin;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

/**
 * Scans a {@link Plugin}'s classpath for {@link PluginConfiguration}, creating the configurations and then
 * adding them as beans to the plugin application context.
 * <p>
 * TODO(rz): This could probably be done in a more idiomatic way by attaching a FactoryBean to the
 *  BeanDefinition, deferring instance creation to Spring, but this was easiest.
 */
public class PluginConfigurationRegisteringCustomizer implements PluginApplicationContextCustomizer {

    private final ConfigFactory configFactory;
    private ClassResolver classResolver;

    public PluginConfigurationRegisteringCustomizer(ConfigFactory configFactory) {
        this(configFactory, null);
    }

    public PluginConfigurationRegisteringCustomizer(ConfigFactory configFactory, ClassResolver classResolver) {
        this.configFactory = configFactory;
        this.classResolver = classResolver;
    }

    @Override
    public void accept(Plugin plugin, ConfigurableApplicationContext context) {
        ClassResolver resolver = classResolver != null ? classResolver :
                new DefaultClassResolver(plugin.getWrapper().getPluginClassLoader());

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false, context.getEnvironment());
        scanner.setResourceLoader(new PathMatchingResourcePatternResolver(plugin.getWrapper().getPluginClassLoader()));
        scanner.addIncludeFilter(new AnnotationTypeFilter(PluginConfiguration.class));

        scanner.findCandidateComponents(PluginUtils.getBasePackageName(plugin))
                .stream()
                .map(bd -> bd.getBeanClassName())
                .filter(className -> className != null)
                .map(className -> {
                    Class<?> cls = resolver.resolveClassName(className);
                    return configFactory.createPluginConfig(
                            cls,
                            plugin.getWrapper().getPluginId(),
                            cls.getAnnotation(PluginConfiguration.class).value()
                    );
                })
                .filter(config -> config != null)
                .forEach(config -> {
                    String beanName = config.getClass().getSimpleName();
                    beanName = Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);

                    if (context.getBeanFactory().containsBean(beanName)) {
                        // This is pretty janky, but it'll get the job done for now.
                        beanName = beanName + System.nanoTime();
                    }
                    context.getBeanFactory().registerSingleton(beanName, config);
                });
    }

    /**
     * Allows for easier testing.
     */
    public interface ClassResolver {
        /**
         * Resolves a {@link Class} given its class name.
         *
         * @param className The class name to resolve
         */
        Class<?> resolveClassName(String className);
    }

    private class DefaultClassResolver implements ClassResolver {
        private final ClassLoader pluginClassLoader;

        DefaultClassResolver(ClassLoader pluginClassLoader) {
            this.pluginClassLoader = pluginClassLoader;
        }

        @Override
        public Class<?> resolveClassName(String className) {
            return ClassUtils.resolveClassName(className, pluginClassLoader);
        }
    }
}
