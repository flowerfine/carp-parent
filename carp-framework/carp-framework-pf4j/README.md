# Pf4j Framework

plugin 模块，copy [kork](https://github.com/spinnaker/kork) 项目。基于 [pf4j](https://github.com/pf4j/pf4j) 开发，依赖项目列表：

* [pf4j](https://github.com/pf4j/pf4j)
* [pf4j-spring](https://github.com/pf4j/pf4j-spring)
* [pf4j-update](https://github.com/pf4j/pf4j-update)

类似项目：[devops-framework/devops-plugin](https://github.com/bkdevops-projects/devops-framework/tree/master/devops-boot-project/devops-boot-core/devops-plugin)、[devops-boot-starter-plugin](https://bkdevops-projects.github.io/devops-framework/#/starter/devops-boot-starter-plugin)。

相对于 kork 项目，做了如下修改：

* 默认集成 spring。将 `kork-plugins-api` 和 `kork-plugins-spring-api` 合并到一个 module
* 移除内置工具。kork 插件提供了 `httpclient`、`yaml`、`serder`、`servicesdk` 支持，方便 kork 插件使用。carp 移除了这些
