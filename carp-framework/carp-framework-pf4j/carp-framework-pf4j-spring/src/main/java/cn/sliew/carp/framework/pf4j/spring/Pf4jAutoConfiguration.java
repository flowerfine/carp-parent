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
package cn.sliew.carp.framework.pf4j.spring;

import cn.hutool.extra.spring.SpringUtil;
import cn.sliew.carp.framework.pf4j.core.actuator.Pf4jPluginsEndpoint;
import cn.sliew.carp.framework.pf4j.core.config.ConfigFactory;
import cn.sliew.carp.framework.pf4j.core.pf4j.CarpPluginManager;
import cn.sliew.carp.framework.pf4j.core.pf4j.status.SpringPluginStatusProvider;
import cn.sliew.carp.framework.pf4j.core.pf4j.status.SpringStrictPluginLoaderStatusProvider;
import cn.sliew.carp.framework.pf4j.core.pf4j.versions.ApplicationVersionManager;
import cn.sliew.carp.framework.pf4j.core.proxy.aspects.InvocationAspect;
import cn.sliew.carp.framework.pf4j.core.proxy.aspects.LogInvocationAspect;
import cn.sliew.carp.framework.pf4j.core.sdks.SdkFactory;
import cn.sliew.carp.framework.pf4j.core.spring.PluginFrameworkInitializer;
import cn.sliew.carp.framework.pf4j.core.spring.SpringPluginFactory;
import cn.sliew.carp.framework.pf4j.core.spring.SpringPluginService;
import cn.sliew.carp.framework.pf4j.core.update.CarpUpdateMananger;
import cn.sliew.carp.framework.pf4j.core.update.downloader.CompositeFileDownloader;
import cn.sliew.carp.framework.pf4j.core.update.downloader.FileDownloaderProvider;
import cn.sliew.carp.framework.pf4j.core.update.downloader.SupportingFileDownloader;
import cn.sliew.carp.framework.pf4j.core.update.props.PluginRepositoryProperties;
import cn.sliew.carp.framework.pf4j.core.update.release.provider.AggregatePluginInfoReleaseProvider;
import cn.sliew.carp.framework.pf4j.core.update.release.provider.PluginInfoReleaseProvider;
import cn.sliew.carp.framework.pf4j.core.update.release.remote.RemotePluginInfoReleaseCache;
import cn.sliew.carp.framework.pf4j.core.update.release.source.PluginInfoReleaseSource;
import cn.sliew.carp.framework.pf4j.core.update.repository.ConfigurableUpdateRepository;
import cn.sliew.carp.framework.spring.dynamicconfig.DynamicConfigService;
import cn.sliew.carp.framework.spring.version.ServiceVersion;
import cn.sliew.carp.framework.spring.version.SpringPackageVersionResolver;
import cn.sliew.carp.framework.spring.version.VersionResolver;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginFactory;
import org.pf4j.PluginStatusProvider;
import org.pf4j.VersionManager;
import org.pf4j.update.UpdateRepository;
import org.pf4j.update.verifier.CompoundVerifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@AutoConfigureAfter(RemotePluginsConfiguration.class)
public class Pf4jAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(VersionResolver.class)
    public static VersionResolver versionResolver() {
        return new SpringPackageVersionResolver();
    }

    @Bean
    @ConditionalOnMissingBean(ServiceVersion.class)
    public static ServiceVersion serviceVersion(
            List<VersionResolver> versionResolvers) {
        return new ServiceVersion(versionResolvers);
    }

    @Bean
    public static VersionManager versionManager() {
        return new ApplicationVersionManager(
                Objects.requireNonNull(SpringUtil.getApplicationName()));
    }

    @Bean
    public static SpringPluginStatusProvider pluginStatusProvider(
            DynamicConfigService dynamicConfigService) {
        String configNamespace = PluginsConfigurationProperties.CONFIG_NAMESPACE;
        String defaultRootPath = PluginsConfigurationProperties.DEFAULT_ROOT_PATH;
        return new SpringPluginStatusProvider(
                dynamicConfigService, configNamespace + "." + defaultRootPath);
    }

    @Bean
    public static PluginFactory pluginFactory(
            List<SdkFactory> sdkFactories,
            ConfigFactory configFactory,
            GenericApplicationContext applicationContext) {
        return new SpringPluginFactory(sdkFactories, configFactory, applicationContext);
    }


    @Bean
    public static CarpPluginManager pluginManager(
            ServiceVersion serviceVersion,
            VersionManager versionManager,
            PluginStatusProvider pluginStatusProvider,
            ConfigFactory configFactory,
            List<SdkFactory> sdkFactories,
            PluginFactory pluginFactory,
            PluginsConfigurationProperties pluginsConfigurationProperties) {
        return new CarpPluginManager(
                serviceVersion,
                versionManager,
                pluginStatusProvider,
                configFactory,
                sdkFactories,
                pluginFactory,
                determineRootPluginPath(pluginsConfigurationProperties));
    }

    /**
     * If the plugins-root-path property is set, returns the absolute path to the property. Otherwise,
     * returns the default root path 'plugins'.
     */
    private static Path determineRootPluginPath(
            PluginsConfigurationProperties pluginsConfigurationProperties) {
        return pluginsConfigurationProperties
                .getPluginsRootPath()
                .equals(PluginsConfigurationProperties.DEFAULT_ROOT_PATH)
                ? Paths.get(PluginsConfigurationProperties.DEFAULT_ROOT_PATH)
                : Paths.get(pluginsConfigurationProperties.getPluginsRootPath()).toAbsolutePath();
    }


    /**
     * Not a static bean - see {@link RemotePluginsConfiguration}.
     */
    @Bean
    @ConditionalOnProperty(
            value = "carp.framework.pf4j.remote-plugins.cache.enabled",
            havingValue = "true",
            matchIfMissing = true)
    public RemotePluginInfoReleaseCache remotePluginInfoReleaseCache(
            Collection<PluginInfoReleaseSource> pluginInfoReleaseSources,
            SpringStrictPluginLoaderStatusProvider springStrictPluginLoaderStatusProvider,
            ApplicationEventPublisher applicationEventPublisher,
            CarpUpdateMananger updateManager,
            CarpPluginManager pluginManager,
            SpringPluginStatusProvider springPluginStatusProvider) {
        return new RemotePluginInfoReleaseCache(
                new AggregatePluginInfoReleaseProvider(
                        pluginInfoReleaseSources.stream()
                                .collect(Collectors.toList()),
                        springStrictPluginLoaderStatusProvider),
                applicationEventPublisher,
                updateManager,
                pluginManager,
                springPluginStatusProvider);
    }

    @Bean
    public static CarpUpdateMananger pluginUpdateManager(
            CarpPluginManager pluginManager,
            ApplicationEventPublisher applicationEventPublisher,
            List<UpdateRepository> updateRepositories) {
        return new CarpUpdateMananger(applicationEventPublisher, pluginManager, updateRepositories);
    }

    @Bean
    public static FileDownloaderProvider fileDownloaderProvider(
            List<SupportingFileDownloader> fileDownloaders) {
        return new FileDownloaderProvider(new CompositeFileDownloader(fileDownloaders));
    }

    @Bean
    @SneakyThrows
    public static List<UpdateRepository> pluginUpdateRepositories(
            Map<String, PluginRepositoryProperties> pluginRepositoriesConfig,
            FileDownloaderProvider fileDownloaderProvider,
            PluginsConfigurationProperties properties) {

        List<UpdateRepository> repositories =
                pluginRepositoriesConfig.entrySet().stream()
                        .filter(entry -> entry.getValue().isEnabled())
                        .map(
                                entry ->
                                        new ConfigurableUpdateRepository(
                                                entry.getKey(),
                                                entry.getValue().getUrl(),
                                                fileDownloaderProvider.get(entry.getValue().getFileDownloader()),
                                                new CompoundVerifier()))
                        .collect(Collectors.toList());

        if (repositories.isEmpty()) {
            log.warn(
                    "No remote repositories defined, will fallback to looking for a "
                            + "'repositories.json' file next to the application executable");
        }

        return repositories;
    }

    @Bean
    public static SpringStrictPluginLoaderStatusProvider springStrictPluginLoaderStatusProvider(
            Environment environment) {
        return new SpringStrictPluginLoaderStatusProvider(environment);
    }

    @Bean
    public static LogInvocationAspect logInvocationAspect() {
        return new LogInvocationAspect();
    }

    @Bean
    public static Pf4jPluginsEndpoint installedPluginsEndpoint(
            CarpPluginManager pluginManager) {
        return new Pf4jPluginsEndpoint(pluginManager);
    }

    @Bean
    public SpringPluginService spinnakerPluginService(
            CarpPluginManager pluginManager,
            CarpUpdateMananger updateManager,
            PluginInfoReleaseProvider pluginInfoReleaseProvider,
            SpringPluginStatusProvider springPluginStatusProvider,
            ApplicationEventPublisher applicationEventPublisher,
            List<InvocationAspect> invocationAspects) {
        return new SpringPluginService(
                pluginManager,
                updateManager,
                pluginInfoReleaseProvider,
                springPluginStatusProvider,
                invocationAspects,
                applicationEventPublisher);
    }

    @Bean
    PluginFrameworkInitializer pluginFrameworkInitializer(SpringPluginService pluginService) {
        return new PluginFrameworkInitializer(pluginService);
    }
}
