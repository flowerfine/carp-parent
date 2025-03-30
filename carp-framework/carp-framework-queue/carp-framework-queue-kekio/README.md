# Queue Frameowrk Kekio

[Spinnaker](https://github.com/spinnaker) 中 [Orca](https://github.com/spinnaker/orca) 服务的 kekio 延迟队列。orca 相关依赖使用 springboot 2.7x，与 carp 使用的 springboot 3.3x 依赖存在兼容问题，无法直接加入 kekio 依赖至 pom.xml 中使用，需迁移过来。

Kekio 并不是一个通用的延迟队列库实现，它专为 [Orca](https://github.com/spinnaker/orca) 而设计，但仍然是一个良好的经过生产验证的延迟队列库。它具有以下缺陷：

* API 定义不够通用。存在部分 API 为 Orca 功能设计
* 消息序列化。强依赖 [Jackson](https://github.com/FasterXML/jackson)，在内部处理延迟消息时使用的是 lua 脚本，内部有解析 json 的行为，限定了 redis 中存储的消息体必需为 json。在路由延迟消息至 `MessageHandler` 时，依赖 `Message` 实现类名，`Message` 实现类依赖 jackson 序列化。
* 应用共享。在 Orca 中整个应用（支持集群部署）共享一个队列，不同的 `Message` 会按照具体的 `Message.class` 路由到不同的 `MessageHandler`。如果想达到像 `RocketMQ` 或 `Pulsar` 类似的消息队列，不同的业务使用不同的 topic，topic 内的消息都是同一类，需在应用中创建多个 Kekio Queue 实例。创建多个 Kekio Queue 实例又会造成一些 metrics 指标采集重复

## 使用指南

- 配置文件
  - 增加 `carp.framework.queue.kekio` 配置。具体查看 `KekioQueueAutoConfiguration` 和 `KekioObjectMapperConfiguration`
- 添加 `@EnableKekioQueue` 注解

### Mem

```yaml
carp.framework:
  queue.kekio:
    enabled: true
    name: kekio
    type: JEDIS
    object-mapper:
      messagePackages:
        - cn.sliew.carp.framework.queue.kekio.message
        - cn.sliew.quoll.kekio.service.message   # 自定义 message package 路径
```

### Redis

#### Standalone

增加配置

```yaml
# standalone
carp.framework:
  queue.kekio:
    enabled: true
    name: kekio
    type: JEDIS
    object-mapper:
      messagePackages:
        - cn.sliew.carp.framework.queue.kekio.message
        - cn.sliew.quoll.kekio.service.message   # 自定义 message package 路径
```

增加 Jedis 声明

```java
@Configuration
@AutoConfigureBefore(KekioQueueAutoConfiguration.class)
public class KekioConfig {

    @Bean
    public JedisPool jedisPool() {
        GenericObjectPoolConfig<Jedis> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setJmxNameBase("jedisPool");
        poolConfig.setJmxNamePrefix("jedis");
        poolConfig.setJmxEnabled(false);
        poolConfig.setMinIdle(1);
        poolConfig.setMaxIdle(3);
        poolConfig.setMaxTotal(10);

        HostAndPort hostAndPort = new HostAndPort("localhost", 6379);
        JedisClientConfig config = DefaultJedisClientConfig.builder()
                .clientName("kekio")
                .database(3)
                .password("123456")
                .build();
        return new JedisPool(poolConfig, hostAndPort, config);
    }
}
```

#### redis-cluster

增加配置

```yaml
# redis-cluster
carp.framework:
  queue.kekio:
    enabled: true
    name: kekio
    type: JEDIS_CLUSTER
    object-mapper:
      messagePackages:
        - cn.sliew.carp.framework.queue.kekio.message
        - cn.sliew.quoll.kekio.service.message   # 自定义 message package 路径
```

同样增加 JedisCluster 声明

## Kekio介绍

Kekio 是一个分布式延迟队列库，支持 at-least-once 投递，属于 [Spinnaker](https://github.com/spinnaker) 项目的一部分，用于 [Orca](https://github.com/spinnaker/orca) 作为内部的队列服务。

Kekio 一开始有一个独立的仓库：[Kekio](https://github.com/spinnaker/keiko)，现在已经是只读状态，代码也迁移到了 [Orca](https://github.com/spinnaker/orca) 仓库中。但是 Kekio 部分代码变动不大。

kekio 提供了 3 种实现：

* memory。基于 JDK DelayQueue 实现。
* redis。基于 [jedis](https://github.com/redis/jedis) 实现，支持 standalone 和 redis-cluster 2 种模式。
* jdbc。支持 MySQL 和 PostgreSQL

本模块支持 memory 和 redis 2 种实现，不支持 jdbc。

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
