# Pf4j Framework

plugin 模块，copy [kork](https://github.com/spinnaker/kork) 项目。基于 [pf4j](https://github.com/pf4j/pf4j) 开发，依赖项目列表：

* [pf4j](https://github.com/pf4j/pf4j)
* [pf4j-spring](https://github.com/pf4j/pf4j-spring)
* [pf4j-update](https://github.com/pf4j/pf4j-update)

类似项目：[devops-framework/devops-plugin](https://github.com/bkdevops-projects/devops-framework/tree/master/devops-boot-project/devops-boot-core/devops-plugin)、[devops-boot-starter-plugin](https://bkdevops-projects.github.io/devops-framework/#/starter/devops-boot-starter-plugin)。

相对于 kork 项目，做了如下修改：

* 默认集成 spring。将 `kork-plugins-api` 和 `kork-plugins-spring-api` 合并到一个 module
* 移除内置工具。kork 插件提供了 `httpclient`、`yaml`、`serder`、`servicesdk` 支持，方便 kork 插件使用。carp 移除了这些

## 核心概念

### Pf4j

* **ExtensionPoint**。`ExtensionPoint` 是一个 Java 标记接口，任意接口或抽象类都可以通过 `ExtensionPoint` 标记为扩展点，应用程序通过扩展点调用自定义接口或抽象类实现。
* **Extension**。`Extension` 是一个注解，标记在 `ExtensionPoint` 实现类上。Extension 为 ExtensionPoint 的一个实现
* **Plugin**。一组 `Extension`。每个 `Plugin` 都由单独的类加载器加载避免 class 冲突。

### Kork

kork 基于 pf4j 开发，增强了 pf4j-spring 和 pf4j-update 功能

* **Bundle**。一组 `Plugin`
* **SDK**。spinnaker 插件开发中常用的类库和工具。包括 `httpclient`、`yaml`、`serder`、`servicesdk`

#### Rmote vs. JVM ExtensionPoint

插件即可以运行在 JVM 内通过方法调用，也可以通过 RPC 调用。

#### PluginRef

本地开发支持

#### V1 vs. V2

v1 版本

v2 版本

#### Update

todo

#### API vs. Spring API

API 方式使用 pf4j 支持。

Spring API 方式支持额外扫描 bean，加载 spring 配置类：

* 这里的 spring 指的是 plugin `ApplicationContext`。plugin 内部启动了一个新的 `ApplicationContext`，并将应用程序的 spring `ApplicationContext` 设置为 `parent`。
  * 通过 `ClassLoader` 和 `SpringContext` 隔离避免 class 冲突和 bean 冲突的思路，可以参考：[1.3.1 架构原理](https://koupleless.io/docs/introduction/architecture/arch-principle/)
* 标记 `@ExposeToApp` 和 `@RestController` 的类会通过注册为 bean。
* 可直接提供 class，注册到 spring 中。



## 开发方式

* `ExtensionPoint`。定义接口，标记为 `ExtensionPoint`。提供 `ExtensionPoint` 标记接口实现
* `@Extension`。定义接口，接口不标记为 `ExtensionPoint`。实现类添加 `@Extension` 注解和 `ExtensionPoint` 接口

使用指南

* 应用程序。
  * 添加依赖。向应用程序中添加依赖启用插件功能
  * 添加配置。在主应用程序中引入配置 `PluginsAutoConfiguration`
* API 模块。保持最小依赖。可以包含接口、POJO，也可以包含 `api`、`implementation` 和 `runtime` 等模块都需要用到的依赖。
  * 定义 `ExtensionPoint` 接口

## 资源信息

* spinnaker
  * [Plugin Creator Guide](https://spinnaker.io/docs/guides/developer/plugin-creator/)
  * [Backend Service Extension Points](https://spinnaker.io/docs/guides/developer/plugin-creator/plugin-backend/)
  * [spinnaker-plugin-examples](https://github.com/spinnaker-plugin-examples)
