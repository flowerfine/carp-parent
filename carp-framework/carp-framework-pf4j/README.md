# Pf4j Framework

plugin 模块，基于 [pf4j](https://github.com/pf4j/pf4j) 开发，copy [kork](https://github.com/spinnaker/kork) 项目。

## 开发方式

引用 kork 推荐的插件结构，保持 core 服务和插件可单独迭代：

* API 模块。定义 `{service}-api`  模块，严格确保 API 模块依赖干净。如果 API、Core 和其他模块都需要的依赖，可以放入 API 模块
  * 引入 [carp-framework-pf4j-api](./carp-framework-pf4j-api) 模块
  * 定义插件接口和 POJO，插件接口继承 `CarpExtensionPoint` 接口
  * 可以引入的公共依赖如 `slf4j-api`、`lombok`，避免引入 `spring` 和 `jackson` 模块，不可避免时设置依赖为 `provided`。
* Core 模块。定义 `{service}-core` 模块。
  * 引入 `{service}-api` 模块和 [carp-framework-pf4j-spring](./carp-framework-pf4j-spring) 模块
  * 配置插件
    * 假设插件名为 `cn.sliew.carp-plugin-test-1`和 `cn.sliew.carp-plugin-test-2`，添加配置，启用插件

```yaml
carp.framework:
  pf4j.plugins:
    cn.sliew.carp-plugin-test-1:
      enabled: true
    cn.sliew.carp-plugin-test-2:
      enabled: true
```

使用 example 可参考 [carp](https://github.com/flowerfine/carp)：

* API 模块。[carp-module-plugin-test-api](https://github.com/flowerfine/carp/tree/dev/carp-modules/carp-module-plugin/carp-module-plugin-test-api)
* Plugins 模块。[carp-plugin-test](https://github.com/flowerfine/carp/tree/dev/carp-plugins/carp-plugin-test)
* Core 模块。[carp-module-plugin-core](https://github.com/flowerfine/carp/tree/dev/carp-modules/carp-module-plugin/carp-module-plugin-core)
* Application 模块。[carp-server](https://github.com/flowerfine/carp/tree/dev/carp-server)

## 核心概念

### Pf4j

* **ExtensionPoint**。`ExtensionPoint` 是一个 Java 标记接口，任意接口或抽象类都可以通过 `ExtensionPoint` 标记为扩展点，应用程序通过扩展点调用自定义接口或抽象类实现。
* **Extension**。`Extension` 是一个注解，标记在 `ExtensionPoint` 实现类上。Extension 为 ExtensionPoint 的一个实现
* **Plugin**。一组 `Extension`。每个 `Plugin` 都由单独的类加载器加载避免 class 冲突。

### Kork

kork 基于 pf4j 开发，着重增强了开发体验和使用体验。

#### SDK

在开发插件时，依赖具体实现，插件会添加自身需要的依赖和工具，实现自己的业务逻辑。为简化插件开发，kork 可以支持为插件类 `Plugin` 或 `Extension` 注入常用工具：

*  `httpclient`
* `yaml`
* `serder`
* `servicesdk`

#### PluginRef

在开发插件时，pf4j 体验不佳，需经历：打包插件 -> 将打包插件放入插件目录 -> 启动应用 -> 测试插件，流程较为繁琐。

kork 提供了 `PluginRef` 功能，用户可定义 [test.plugin-ref](https://github.com/spinnaker/kork/blob/master/kork-plugins/src/test/resources/test.plugin-ref) 文件，指定插件 [testplugin/plugin.properties](https://github.com/spinnaker/kork/blob/master/kork-plugins/src/test/resources/testplugin/plugin.properties) 位置。应用只需提供 `.plugin-ref` 和 `.properties` 文件，无需重新打包插件 -> 将打包插件放入插件目录，可直接启动应用，测试插件。

如 pf4j 默认的插件目录是 `plugins`。在打包项目的时候可以通过 `maven-assembly-plugin` 插件生成 `plugins` 目录，并将项目中的插件实现打包放入 `plugins` 目录。最后生成的 `xxx-bin.tar.gz` 包解压后就可以看到 `plugins` 目录和项目内置的插件。用户也随时可以像 `plugins` 目录新增新的插件实现，重启使插件生效。

开发阶段，通过 `PluginRef` 功能，用户可以项目代码中创建一个 `plugins` 目录，内部放入 `test.plugin-ref` 文件，并在 `plugins` 目录下创建 `testplugin/plugin.properties` 文件。之后无需经过打包、启动项目、测试插件的流程，直接启动 IDEA，就可以看到项目成功加载插件。

示例项目结构如下，开发阶段 `carp-plugin-test-1` 和 `carp-plugin-test-2` 只需在项目根目录创建 `plugins`，添加 `.plugin-ref` 文件。但是需将 `carp-plugin-test-1` 和 `carp-plugin-test-2` 放入项目启动模块的 `pom.xml` 中，release 阶段需移除，正式环境下通过插件方式加载。

```
carp
├── carp-dist
│   ├── pom.xml
│   └── src
│       ├── assembly
│       │   └── carp-dist.xml
│       └── bin
│           ├── carp.sh
│           └── config.sh
├── carp-modules
│   ├── ...
│   └── carp-module-plugin
├── carp-plugins
│   └── carp-plugin-test
│       ├── carp-plugin-test-api
│       └── carp-plugin-test-plugins
│       	├── carp-plugin-test-1
│       	└── carp-plugin-test-2
├── carp-server
├── plugins
│   ├── test-plugin-1
│   │   └── plugin.properties
│   └── test-plugin-1.plugin-ref
└── pom.xml
```

release 阶段，依然通过 `maven-assembly-plugin` 生成 `plugins` 目录。

#### Unsafe

kork 提供了 `unsafe` 概念。在 pf4j 和 kork 中均对 class 隔离做了支持，避免 class 冲突。

* pf4j
  * 每个插件使用单独的 `ClassLoader` 加载插件，避免 class 冲突。
* kork。
  * 每个插件使用单独的 `ClassLoader` 加载插件，避免 class 冲突。
  * 每个插件使用单独的 spring `ApplicationContext`，避免 bean 冲突。kork 支持将 Extension 注册为 spring 中 bean。kork 会设置应用本身的 `ApplicationContext` 为插件的 `ApplicationContext` 的 `parent`。
  * 通过 `ClassLoader` 和 `SpringContext` 隔离避免 class 冲突和 bean 冲突的思路，可以参考：[1.3.1 架构原理](https://koupleless.io/docs/introduction/architecture/arch-principle/)。

pf4j 和 kork 都保证了 class 安全，kork 额外保证了 bean 安全。默认情况下插件是 **safe** 的，不存在 class 冲突。

当插件被标记为 `unsafe` 时即表明不为插件创建单独的 `ClassLoader`，转而使用应用的 `ClassLoader` 加载，插件存在一定 class 冲突风险，是 **unsafe** 的。

#### Config

插件也存在配置读取需求。插件配置分为 2 种：

* 插件自带。插件打包时可包含自定义的配置文件如 `.yaml`
* 应用配置。应用在添加插件时，可以在应用配置中添加插件配置，供插件读取

kork 中的配置分为 2 类：Plugin 配置和 Extension 配置。

Plugin 配置为 `Plugin` 配置，Extension 配置，为可注入 `SpinnakerExtensionPoint` 的配置。

##### Plugin

###### `@PluginConfiguration`

定义配置类，标记 `@PluginConfiguration` 注解。

```java
@Data
@PluginConfiguration
public class HelloPluginProperties {

    private String name;
}
```

###### `Plugin`

`Plugin` 实现类，构造器增加 `@PluginConfiguration` 标记类，作为构造器参数。

```java

public class HelloPlugin extends DemoPlugin {

    private PluginSdks pluginSdks;
    private HelloPluginProperties properties;

    public HelloPlugin(PluginWrapper wrapper, PluginSdks pluginSdks, HelloPluginProperties properties) {
        super(wrapper);
        this.pluginSdks = pluginSdks;
        this.properties = properties;
    }

    @Override
    public void start() {
        log.info("HelloPlugin.start(), name: {}", properties.getName());
    }
}
```

###### `application.yaml`

在 `application.yaml` 中新增属性配置，按照路径 `/carp/framework/pf4j/plugins/{pluginId}/config/` 添加

```yaml
carp.framework:
  pf4j.plugins:
    cn.sliew.carp-plugin-test-1:
      enabled: true
      config:
        name: carp-plugin-test-1-name
```

`cn.sliew.carp-plugin-test-1` 为 `pluginId`。应用启动后即可通过日志验证参数配置。

##### Extension

Extension 指 `SpinnakerExtensionPoint` 实现类，在 kork 对 spring 的支持中所有实现 `SpinnakerExtensionPoint` 和标记 `PluginComponent` 类都会自动注册为 spring bean。如果 `SpinnakerExtensionPoint` 添加 `@Extension` 注解，则也会被 pf4j 的 `ExtensionFactory` 创建并实例化。注意这种情况下会被实例化 2 次：kork spring 和 pf4j。kork spring 实例化类可通过依赖注入获取使用，pf4j 实例化类可通过 `PluginManager#getExtension` 获取使用。

###### pf4j

`@Extension` 注解标注的类，也可以通过构造器参数注入配置类。但是在 `application.yaml` 中的配置参数路径需要做调整，改为 `/carp/framework/pf4j/plugins/{pluginId}/extensions/config`，其他的都与 `Plugin` 一致。

```yaml
carp.framework:
  pf4j.plugins:
    cn.sliew.carp-plugin-test-1:
      enabled: true
      config:
        name: carp-plugin-test-1-name
      extensions:
        config:
          name: carp-plugin-test-1-extension-name
```

因为 kork 对 spring 的支持，所有实现 `SpinnakerExtensionPoint` 和标记 `PluginComponent` 注解的类都会自动注册为 spring bean。可以在 `SpinnakerExtensionPoint` 实现类和标记 `PluginComponent` 注解的类中通过依赖注入，注入配置类。当然都是 spring bean，也不必只是注入配置类。

```java
/**
 * 类似 spring bean
 */
@PluginComponent
@RequiredArgsConstructor
public class WelcomeService {
  
    // 自动注入
    private final WelcomePluginWithNamespaceProperties properties;

    public String getGreeting() {
        return "Welcome, " + properties.getValue();
    }
}
```

```java
/**
 * 如果添加 @Extension 注解，pf4j 的 ExtensionFactory 会生成这个实例。
 * 无论添不添加 @Extension，都会因为 CarpExtensionPoint 被实例化，注册到 spring 中。
 * 这里不添加 @Extension 注解，避免 pf4j 的 ExtensionFactory 生成这个实例
 */
@RequiredArgsConstructor
public static class WelcomeGreeting implements Greeting {

    // 自动注入
    private final WelcomeService welcomeService;

    @Override
    public String getGreeting() {
        return welcomeService.getGreeting();
    }
}
```

#### Spring

kork 推荐的插件结构，保持 core 服务和插件可单独迭代：

* API 模块。定义 `{service}-api`  模块，严格确保 API 模块依赖干净。如果 API、Core 和其他模块都需要的依赖，可以放入 API 模块
  * 引入 `plugins-api` 或 `spring-plugins-api` 模块
  * 定义插件接口和 POJO，插件接口继承 `SpinnakerExtensionPoint` 接口
  * 可以引入的公共依赖如 `slf4j-api`、`lombok`，避免引入 `spring` 和 `jackson` 模块，不可避免时设置依赖为 `provided`。
* Core 模块。定义 `{service}-core` 模块。
  * 引入 `{service}-api` 模块和 `plugins` 模块

在定义 `{service}-api` 模块时，可以依赖 `plugins-api` 或 `spring-plugins-api` 模块，`spring-plugins-api` 模块默认增加了注册 Extension 和额外类到 spring 中。注意这里的 spring 是每个插件单独创建的 `ApplicationContext`，它会设置应用的 `ApplicationContext` 作为 `parent`。

kork 的插件框架也经过迭代，有过变更。在 kork v2 版本插件中废弃了 `spring-plugins-api` 的功能（为保证现有插件继续使用，并未直接取消支持或删除 `spring-plugins-api` 模块，仍然兼容，开发者仍然可以继续使用 `spring-plugins-api` 开发插件。但是在 v2 版本中将 `spring-plugins-api` 的功能作为默认功能加以实现，实际上不需要 `spring-plugins-api` 模块）。

在 v2 版本中插件会默认将 `SpinnakerExtensionPoint` 标记的接口实现类都注册为 spring bean，同时标记 `@PluginComponent` 的类也会注册为 spring bean。spring bean 扫描 package 路径为 `MyPlugin` 类包名。

将 `SpinnakerExtensionPoint` 和 `@PluginComponent`注册为 spring bean 后，Extension 也可以使用 spring 的依赖注入功能：

* 插件可自由注入插件实现中定义的 bean
* 插件可自由注入 core 实现的 bean。需在 `{service}-api` 中定义接口，方便插件定义注入

#### Rmote vs. JVM

广义的插件不止运行在 JVM 内，还可以独立部署，与应用通过 RPC 通信。独立部署的插件很难说是插件，还是依赖的三方服务。但是独立部署的三方服务在耦合上来说更低，因为微服务本身就是确保服务之间可以独立迭代，服务与服务之间通过协议交互（HTTP、RPC）。

kork 也提供 RemotePlugin。

#### Update

不支持。kork 并未支持应用不停机更新插件，kork 推荐重启或重新部署应用重新加载插件达到更新插件目的。

插件在线更新其实是件危险的事情。pf4j 和 kork 都不支持 replace 插件，pf4j 提供的是销毁旧插件，启动新插件。插件 ExtensionPoint 会被代码使用，销毁插件其实是释放 ClassLoader，可能导致应用在插件销毁后处理业务时遇到错误。

## 参考资料

pf4j 项目列表：

* [pf4j](https://github.com/pf4j/pf4j)。插件接口，实现插件加载、卸载、启用、禁用、获取 Extension
* [pf4j-spring](https://github.com/pf4j/pf4j-spring)。将 Extension 注册为 spring bean
* [pf4j-update](https://github.com/pf4j/pf4j-update)。应用不重启，实现插件的动态更新

类似项目：

* [sbp](https://github.com/hank-cp/sbp)
* [exp](https://github.com/stateIs0/exp)
* [devops-framework](https://github.com/bkdevops-projects/devops-framework)。
  * [devops-framework/devops-plugin](https://github.com/bkdevops-projects/devops-framework/tree/master/devops-boot-project/devops-boot-core/devops-plugin)
  * [devops-boot-starter-plugin](https://bkdevops-projects.github.io/devops-framework/#/starter/devops-boot-starter-plugin)
* [spring-plugin](https://github.com/spring-projects/spring-plugin)。本来已经停更了，但是又重新准备发版了。
* [easy-extension](https://github.com/xiaoshicae/easy-extension)
* [COLA](https://github.com/alibaba/COLA)。源码：[cola-components/cola-component-extension-starter](https://github.com/alibaba/COLA/tree/master/cola-components/cola-component-extension-starter)
* [AGEIPort](https://github.com/alibaba/AGEIPort)。源码：[ageiport-ext/ageiport-ext-arch](https://github.com/alibaba/AGEIPort/tree/master/ageiport-ext/ageiport-ext-arch)。类似 dubbo 的 spi 机制

kork 项目为 spinnaker 提供了服务端插件解决方案，基于 pf4j 开发：

* [Plugin Creator Guide](https://spinnaker.io/docs/guides/developer/plugin-creator/)
* [Backend Service Extension Points](https://spinnaker.io/docs/guides/developer/plugin-creator/plugin-backend/)
* [spinnaker-plugin-examples](https://github.com/spinnaker-plugin-examples)
