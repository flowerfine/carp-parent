package cn.sliew.carp.framework.pf4j.spring;

import cn.sliew.carp.framework.pf4j.core.config.ConfigCoordinates;
import cn.sliew.carp.framework.pf4j.core.config.ConfigFactory;
import cn.sliew.carp.framework.pf4j.core.config.ConfigResolver;
import cn.sliew.carp.framework.pf4j.core.config.SpringEnvironmentConfigResolver;
import cn.sliew.carp.framework.pf4j.core.update.props.PluginRepositoryProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

public class ConfigAutoConfiguration {

    @Bean
    PluginsConfigurationProperties pluginsConfigurationProperties(Environment environment) {
        return Binder.get(environment)
                .bind(PluginsConfigurationProperties.CONFIG_NAMESPACE, PluginsConfigurationProperties.class)
                .orElseGet(PluginsConfigurationProperties::new);
    }

    @Bean
    @ConditionalOnMissingBean(ConfigResolver.class)
    public static ConfigResolver springEnvironmentConfigResolver(
            ConfigurableEnvironment environment) {
        return new SpringEnvironmentConfigResolver(environment);
    }

    @Bean
    ConfigFactory configFactory(ConfigResolver configResolver) {
        return new ConfigFactory(configResolver);
    }

    @Bean
    public static Map<String, PluginRepositoryProperties> pluginRepositoriesConfig(
            ConfigResolver configResolver) {
        return configResolver.resolve(
                new ConfigCoordinates.RepositoryConfigCoordinates(),
                new TypeReference<HashMap<String, PluginRepositoryProperties>>() {});
    }
}
