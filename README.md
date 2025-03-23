[![Gihub Actions](https://github.com/flowerfine/carp-parent/actions/workflows/ci.yml/badge.svg?branch=dev)](https://github.com/flowerfine/carp-parent/actions) [![Last commit](https://img.shields.io/github/last-commit/flowerfine/carp-parent.svg)](https://github.com/flowerfine/carp-parent) [![GitHub Tag](https://img.shields.io/github/v/tag/flowerfine/carp-parent)](https://github.com/flowerfine/carp-parent/tags) [![Maven Central](https://img.shields.io/maven-central/v/cn.sliew/carp-parent)](https://maven-badges.herokuapp.com/maven-central/cn.sliew/carp-parent) [![License](https://img.shields.io/github/license/flowerfine/carp-parent.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

# Carp-Parent

carp-parent 为 [carp](https://github.com/flowerfine/carp) 项目提供统一的依赖管理，确保依赖全局一致。

## 使用方式

#### Maven’s Bill of Material (BOM)

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>cn.sliew</groupId>
            <artifactId>carp-dependencies</artifactId>
            <version>${latest}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 基于继承关系的Maven父依赖

```xml
<parent>
    <groupId>cn.sliew</groupId>
    <artifactId>carp-spring-boot-parent</artifactId>
    <version>${latest}</version>
    <relativePath/>
</parent>
```

## Carp Framework

framework 提供常见功能模块以满足快速开发需求

* 基础功能
  * [carp-framework-exception](./carp-framework/carp-framework-exception)。异常处理
  * [carp-framework-common](./carp-framework/carp-framework-common)。通用模块、工具类
  * [carp-framework-biz](./carp-framework/carp-framework-biz)。jackson、mapstruct、mybatis、validation、fastexcel 等常用框架扩展
  * [carp-framework-spring](./carp-framework/carp-framework-spring)
* 存储
  * [carp-framework-mongo](./carp-framework/carp-framework-mongo)
  * [carp-framework-mybatis](./carp-framework/carp-framework-mybatis)
  * [carp-framework-redis](./carp-framework/carp-framework-redis)
* 微服务
  * [carp-framework-id](./carp-framework/carp-framework-id)。唯一id
  * [carp-framework-lock](./carp-framework/carp-framework-lock)。提供基于 [ShedLock](https://github.com/lukas-krecan/ShedLock) 和 [redisson](https://github.com/redisson/redisson) 锁实现
  * [carp-framework-feign](./carp-framework/carp-framework-feign)。http 调用
  * [carp-framework-socketio](./carp-framework/carp-framework-socketio)。WebSocket
* web 应用
  * [carp-framework-web](./carp-framework/carp-framework-web)
* [carp-framework-log](./carp-framework/carp-framework-log)。提供多场景日志解决方案
  * 接口请求日志（类似用户行为日志）
  * 数据修改日志。详细记录应用关键数据的变动：增、删、改（改涉及到字段级的增、删、改）
  * 权限审计日志。账号（开通、关闭、注销），权限（授权、取消授权），鉴权（认证成功、失败），登录登出等
  * 实时日志。调度系统实时展示运行日志，Flink 实时数据查询等

* [carp-framework-dag](./carp-framework/carp-framework-dag)
* [carp-framework-license](./carp-framework/carp-framework-license)
* [carp-framework-pekko](./carp-framework/carp-framework-pekko)
* [carp-framework-pf4j](./carp-framework/carp-framework-pf4j)
* [carp-framework-pubsub](./carp-framework/carp-framework-pubsub)
* [carp-framework-task](./carp-framework/carp-framework-task)

## 项目发版

参考：[Release](./docs/Release.md)

## Code of Conduct

This project adheres to the Contributor Covenant [code of conduct](https://www.contributor-covenant.org/version/2/1/code_of_conduct/)

## Contributing

For contributions, please refer [CONTRIBUTING](https://github.com/flowerfine/carp)

Thanks for all people who already contributed to Carp!

<a href="https://github.com/flowerfine/carp/graphs/contributors">
    <img src="https://contrib.rocks/image?repo=flowerfine/carp" /></a>

## Contact

* Bugs and Features: [Issues](https://github.com/flowerfine/carp/issues)

## License

Carp is licenced under the Apache License Version 2.0, link is [here](https://www.apache.org/licenses/LICENSE-2.0.txt).