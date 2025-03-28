# Queue Frameowrk Kekio

[spinnaker](https://github.com/spinnaker) 中 [orca](https://github.com/spinnaker/orca) 服务的 kekio 延迟队列。orca 相关依赖使用 springboot 2.7x，与 carp 使用的 springboot 3.3x 依赖存在兼容问题，无法直接加入 kekio 依赖至 pom.xml 中使用，需迁移过来。

## 使用指南



## Kekio介绍

Kekio 是一个分布式延迟队列库，支持 at-least-once 投递，属于 [Spinnaker](https://github.com/spinnaker) 项目的一部分，用于 [Orca](https://github.com/spinnaker/orca) 作为内部的队列服务。

Kekio 一开始有一个独立的仓库：[Kekio](https://github.com/spinnaker/keiko)，现在已经是只读状态，代码也迁移到了 [Orca](https://github.com/spinnaker/orca) 仓库中。但是 Kekio 部分代码变动不大。

Kekio 并不是一个通用的延迟队列实现，它专为 [Orca](https://github.com/spinnaker/orca) 而设计，消息序列化、API 定义不够通用。

kekio 提供了 3 种实现：

* memory。基于 JDK DelayQueue 实现。
* redis。基于 [jedis](https://github.com/redis/jedis) 实现，支持 standalone 和 redis-cluster 2 种模式。
* jdbc。支持 MySQL 和 PostgreSQL

本模块支持 memory 和 redis 2 种实现，不支持 jdbc

### 核心概念

#### `Queue`

`Queue` 接收**不重复**消息，接收消息时可以指定延迟投递时间。`Queue` 对每条 `Message` 进行 hash （默认不包含 attributes）得到消息 Fingerprint，确保消息不重复。

#### `QueueProcessor`

`QueueProcessor` 从 `Queue` 中不断 `poll` 消息，将 polled 消息按照消息类型（Class）推送给对应的 `MessageHandler`。`QueueProcessor` 使用单线程 poll 消息，使用线程池推送给 `MessageHandler`。`MessageHandler` 执行的线程池为 `QueueExecutor`，推送 `Message` 时会检测线程池容量，如果线程池已满不会推送消息，避免客户端崩溃

#### `Message`

用户需定义消息类，继承 `Message`，同时需在消息类上指定 `@JsonTypeName` 注解。Kekio 内部使用 `jackson` 作为消息序列化库，消息的序列化和反序列化都是通过 `jackson` 实现。因此用户需添加 `@JsonTypeName` 注解支持消息序列化和反序列化。

消息类应该足够简单且不可变，如 Kotlin data 类，Java POJO 等都是比较好的实现方式。如果需要修改消息信息，可通过消息的 `attributes` 属性，传递可变属性。`attributes` 需实现 `Attribute` 接口，实现类同样需添加 `@JsonTypeName` 注解以支持 `jackson` 序列化和反序列化。

注意：在计算消息 Fingerprint 时会先移除 `attributes` 属性确保消息 Fingerprint 不变。

#### `MessageHandler`

`MessageHandler` 可以处理消息类型指定的 Class 及其子类。`MessageHandler` 实现时需确保处理速度足够快，如果处理时间超过 `ackTimeout` 或者异常抛出，消息会重新推入 `Queue`

### 使用说明

在使用 Kekio 时，定义 `Message` 或 `Attribute` 类后，需通过配置通知 Kekio 有新的实现类，Kekio 内部通过代码扫描对应的实现类，注册至内部使用的 `ObjectMapper` 以正确序列化和反序列化。

定义 `MessageHandler` 需注册为 spring bean，以确保系统能正确加载 `MessageHandler`。
