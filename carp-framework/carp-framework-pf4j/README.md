# Pf4j Framework

plugin 模块，基于 [pf4j](https://github.com/pf4j/pf4j) 开发，copy [kork](https://github.com/spinnaker/kork) 项目。

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

在开发插件时，pf4j 体验不佳，需用户打包插件 -> 将打包插件放入插件目录 -> 启动应用 -> 测试插件，流程较为繁琐。

kork 提供了 `PluginRef` 功能，用户可定义 [test.plugin-ref](https://github.com/spinnaker/kork/blob/master/kork-plugins/src/test/resources/test.plugin-ref) 文件，指定插件 [testplugin/plugin.properties](https://github.com/spinnaker/kork/blob/master/kork-plugins/src/test/resources/testplugin/plugin.properties) 位置。应用只需提供 `.plugin-ref` 和 `.properties` 文件，无需重新打包插件 -> 将打包插件放入插件目录，可直接启动应用，测试插件。

#### Unsafe

kork 提供了 `unsafe` 概念。在 pf4j 中，每个插件使用单独的 `ClassLoader` 加载插件，避免 class 冲突。kork 支持将 Extension 注册为 spring 中 bean，为每个插件创建了 `ApplicationContext`，设置应用本身的 `ApplicationContext` 为插件的 `ApplicationContext` 的 `parent`。通过 `ClassLoader` 和 `SpringContext` 隔离避免 class 冲突和 bean 冲突的思路，可以参考：[1.3.1 架构原理](https://koupleless.io/docs/introduction/architecture/arch-principle/)。

pf4j 和 kork 都保证了 class 安全，kork 额外保证了 bean 安全。默认情况下插件是 **safe** 的。

kork 支持使用应用本身的 `ClassLoader` 加载 plugin，这种方式存在一定 class 冲突风险。当插件被标记为 `unsafe` 时即表明不为插件创建单独的 `ClassLoader`，转而使用应用的 `ClassLoader` 加载。

#### Config

插件也存在配置读取需求。插件配置分为 2 种：

* 插件自带。插件打包时可包含自定义的配置文件如 `.yaml`
* 应用配置。应用在添加插件时，可以在应用配置中添加插件配置，供插件读取

todo 提供定义插件配置，定义配置，注入配置

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

不支持。kork 并未支持应用不停机更新插件。

## 开发方式

* `ExtensionPoint`。定义接口，标记为 `ExtensionPoint`。提供 `ExtensionPoint` 标记接口实现
* `@Extension`。定义接口，接口不标记为 `ExtensionPoint`。实现类添加 `@Extension` 注解和 `ExtensionPoint` 接口

使用指南

* 应用程序。
  * 添加依赖。向应用程序中添加依赖启用插件功能
  * 添加配置。在主应用程序中引入配置 `PluginsAutoConfiguration`
* API 模块。保持最小依赖。可以包含接口、POJO，也可以包含 `api`、`core`、`implementation` 和 `runtime` 等模块都需要用到的依赖。
  * 定义 `ExtensionPoint` 接口

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
