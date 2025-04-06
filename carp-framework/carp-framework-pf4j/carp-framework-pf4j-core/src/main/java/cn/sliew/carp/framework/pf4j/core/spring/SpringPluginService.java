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

import cn.sliew.carp.framework.pf4j.api.internal.CarpExtensionPoint;
import cn.sliew.carp.framework.pf4j.api.spring.PrivilegedSpringPlugin;
import cn.sliew.carp.framework.pf4j.core.events.ExtensionCreated;
import cn.sliew.carp.framework.pf4j.core.pf4j.CarpPluginManager;
import cn.sliew.carp.framework.pf4j.core.pf4j.finders.UnsafePluginDescriptor;
import cn.sliew.carp.framework.pf4j.core.pf4j.status.SpringPluginStatusProvider;
import cn.sliew.carp.framework.pf4j.core.proxy.LazyExtensionInvocationProxy;
import cn.sliew.carp.framework.pf4j.core.proxy.aspects.InvocationAspect;
import cn.sliew.carp.framework.pf4j.core.update.CarpPluginInfo;
import cn.sliew.carp.framework.pf4j.core.update.CarpUpdateMananger;
import cn.sliew.carp.framework.pf4j.core.update.release.PluginInfoRelease;
import cn.sliew.carp.framework.pf4j.core.update.release.provider.PluginInfoReleaseProvider;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Plugin;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * A service for managing the plugin framework.
 * <p>
 * NOTE: Over time, we should be moving to this class over {@link CarpPluginManager} and
 * {@link CarpUpdateMananger} as the primary touch points for the plugin framework, decoupling
 * Carp-specific plugin framework logic from PF4J wherever possible.
 */
@Slf4j
public class SpringPluginService {

    private final CarpPluginManager pluginManager;
    private final CarpUpdateMananger updateManager;
    private final PluginInfoReleaseProvider pluginInfoReleaseProvider;
    private final SpringPluginStatusProvider springPluginStatusProvider;
    private final List<InvocationAspect> invocationAspects;
    private final ApplicationEventPublisher applicationEventPublisher;

    private boolean initialized = false;

    public SpringPluginService(
            CarpPluginManager pluginManager,
            CarpUpdateMananger updateManager,
            PluginInfoReleaseProvider pluginInfoReleaseProvider,
            SpringPluginStatusProvider springPluginStatusProvider,
            List<InvocationAspect> invocationAspects,
            ApplicationEventPublisher applicationEventPublisher) {
        this.pluginManager = pluginManager;
        this.updateManager = updateManager;
        this.pluginInfoReleaseProvider = pluginInfoReleaseProvider;
        this.springPluginStatusProvider = springPluginStatusProvider;
        this.invocationAspects = invocationAspects;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Starts the plugin framework and completely initializes extensions for use by the application.
     */
    public void initialize() {
        Assert.isTrue(!initialized, "Plugin framework has already been initialized");

        withTiming("initializing plugins", () -> {
            // Load known plugins prior to downloading so we can resolve what needs to be updated.
            pluginManager.loadPlugins();

            // Find the plugin releases for the currently enabled list of plugins
            List<CarpPluginInfo> pluginInfos = updateManager.getPlugins().stream()
                    .filter(plugin -> springPluginStatusProvider.isPluginEnabled(plugin.id))
                    .map(plugin -> (CarpPluginInfo) plugin)
                    .toList();
            Set<PluginInfoRelease> releases = pluginInfoReleaseProvider.getReleases(pluginInfos);

            // Download releases, if any, updating previously loaded plugins where necessary
            updateManager.downloadPluginReleases(releases).forEach(pluginPath ->
                    pluginManager.loadPlugin(pluginPath)
            );
        });
    }

    /**
     * Start the plugins, attaching exported plugin extensions to the provided {@link BeanDefinitionRegistry}.
     */
    public void startPlugins(BeanDefinitionRegistry registry) {
        withTiming("starting plugins", () -> {
            // Start plugins. This should only be called once.
            pluginManager.startPlugins();

            // Perform additional work for Spring plugins; registering internal classes as beans where necessary
            pluginManager.getStartedPlugins().forEach(pluginWrapper -> {
                Plugin plugin = pluginWrapper.getPlugin();
                if (plugin instanceof PrivilegedSpringPlugin privilegedSpringPlugin) {
                    privilegedSpringPlugin.registerBeanDefinitions(registry);
                }

                if (plugin instanceof PluginContainer container) {
                    String initializerBeanName = container.registerInitializer(registry);
                    registerProxies(container, registry, initializerBeanName);
                }
            });
        });
    }

    /**
     * Registers lazy, proxied bean definitions for a plugin's extensions.
     * This allows service-level beans to inject and use extensions like any other beans.
     * The proxied extensions are initialized via {@link SpringPluginInitializer} when called for the first time.
     */
    private void registerProxies(PluginContainer container, BeanDefinitionRegistry registry, String initializerBeanName) {
        var pluginContext = container.pluginContext;
        String pluginId = container.getWrapper().getDescriptor().getPluginId();

        // Find the plugin's CarpExtensionPoints.
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(CarpExtensionPoint.class));
        scanner.setResourceLoader(new DefaultResourceLoader(container.getWrapper().getPluginClassLoader()));

        scanner.findCandidateComponents(PluginUtils.getBasePackageName(container.getActual())).forEach(extensionBeanDefinition -> {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends CarpExtensionPoint> extensionBeanClass =
                        (Class<? extends CarpExtensionPoint>) container.getWrapper().getPluginClassLoader()
                                .loadClass(extensionBeanDefinition.getBeanClassName());

                // Find the name that the extension bean will (but hasn't yet) be given inside the plugin application context.
                // We'll use this to look up the extension inside the lazy loader.
                String pluginContextBeanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(
                        extensionBeanDefinition,
                        pluginContext
                );

                // Provide an implementation of the extension that can be injected immediately by service-level classes.
                CarpExtensionPoint proxy = (CarpExtensionPoint) LazyExtensionInvocationProxy.proxy(
                        () -> {
                            // Force the plugin's initializer to run if it hasn't already.
                            if (pluginContext.getParent() != null) {
                                pluginContext.getParent().getBean(initializerBeanName);
                            } else {
                                throw new IllegalStateException("Plugin context for \"" + pluginId + "\" was not configured with a parent context");
                            }

                            // Fetch the extension from the plugin context.
                            return (CarpExtensionPoint) pluginContext.getBean(pluginContextBeanName);
                        },
                        extensionBeanClass,
                        invocationAspects,
                        (UnsafePluginDescriptor) container.getWrapper().getDescriptor()
                );

                var proxyBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
                proxyBeanDefinition.setInstanceSupplier(() -> proxy);
                proxyBeanDefinition.setBeanClass(extensionBeanClass);

                String beanName = pluginId + "_" + extensionBeanClass.getSimpleName();
                beanName = Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
                registry.registerBeanDefinition(beanName, proxyBeanDefinition);

                applicationEventPublisher.publishEvent(new ExtensionCreated(
                        this,
                        pluginContextBeanName,
                        proxy,
                        extensionBeanClass
                ));
            } catch (ClassNotFoundException e) {
                log.error("Failed to load extension class", e);
            }
        });
    }

    private void withTiming(String task, Runnable callback) {
        long start = System.currentTimeMillis();

        log.debug(StringUtils.capitalize(task));

        callback.run();

        log.debug("Finished {} in {}ms", task, System.currentTimeMillis() - start);
    }
}
