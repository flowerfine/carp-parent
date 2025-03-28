# Queue Frameowrk

延迟队列框架

框架整合了多种延迟任务实现方式，提供开箱即用的延迟队列：

*  [carp-framework-queue-kekio](./carp-framework-queue-kekio)。迁移 [spinnaker](https://github.com/spinnaker) 中 [orca](https://github.com/spinnaker/orca) 服务的 kekio 延迟队列。orca 相关依赖使用 springboot 2.7x，与 carp 使用的 springboot 3.3x 依赖存在兼容问题，无法直接加入 kekio 依赖至 pom.xml 中使用，需迁移过来

## 延迟队列概览

在 Java 生态中存在多种延迟任务解决方案：

* [RocketMQ](https://rocketmq.apache.org/)。在 5.0 版本中，延迟时间取消了固定值，可设置任意时间，但延迟时间不能超过 24 小时。参考：[定时/延时消息](https://rocketmq.apache.org/zh/docs/featureBehavior/02delaymessage)
* [Kafka](https://kafka.apache.org/)。Kafka 并不支持延迟消息，但是用户可以参照 RocketMQ 实现延迟消息思路，实现 Kafka 版本的固定间隔延迟消息功能
  * [kafka-delayed-queue](https://github.com/cashfree/kafka-delayed-queue)
  * [高吞吐低延迟：朴朴基于 Kafka 的延迟队列实践](https://www.infoq.cn/article/2YMOLi5o2ooj1vW3R3q7?utm_source=related_read&utm_medium=article)
* [Pulsar](https://pulsar.apache.org/)。参考：[Delayed message delivery](https://pulsar.apache.org/docs/4.0.x/concepts-messaging/#delayed-message-delivery)
* Redis。基于 Redis 实现延迟队列
  * [基于Spring Boot实现redis延迟队列](https://mp.weixin.qq.com/s?__biz=MzU4NDc1NDMxMw==&mid=2247487120&idx=1&sn=6fff88f28b24c552b6b4e7951ecd492b&chksm=fce83c7de581c08277b2546a2e9a4783b72dad2cfdf108fcffcb4fd027df587dfcbc09ea3c93&mpshare=1&scene=1&srcid=0214KpE2p4ij1TsNWWPQ2tEZ&sharer_shareinfo=45ec293c8079f7cf60fc0d17d2486427&sharer_shareinfo_first=2dc7df3b1b3c37c7982d8994239ed4a5&version=4.1.10.99312&platform=mac#rd)
  * [redisson](https://github.com/redisson/redisson)。参考：[Delayed Queue](https://redisson.pro/docs/data-and-services/collections/#delayed-queue)
    * [Spring Boot + Redission 自定义分布式延时任务组件设计](https://mp.weixin.qq.com/s/tsinZNPJ8H2QM8hzEd9gMA?version=4.1.10.99312&platform=mac&poc_token=HKW9x2ejDDesDgzdkD5FpCpLReMK0MhmJ4oIu4Lg)
    * [tql-delayqueue](https://github.com/beyondyuefei/tql-delayqueue)。延迟消息框架，延迟消息支持秒级精度、消息高可靠性、水平扩展，底层基于redisson实现
    * [redeq](https://github.com/kevinleeex/redeq)。
    * [delay-queue](https://github.com/Ruffianjiang/delay-queue)
  * [有赞延迟队列设计](https://tech.youzan.com/queuing_delay/)
    * [Redis延时队列的简单实现(基于有赞的设计)](https://mp.weixin.qq.com/s/8diUam1j0fuqfOmGDBTopw?version=4.1.10.99312&platform=mac)
  * [redis-delay-queue](https://github.com/lili101/redis-delay-queue)
  * [DelayingQueue](https://github.com/Barry04/DelayingQueue)
  * [rqueue](https://github.com/sonus21/rqueue)
  * [camellia](https://github.com/netease-im/camellia)
  * [orca](https://github.com/spinnaker/orca)
  * [lmstfy](https://github.com/bitleak/lmstfy)
    * [2G 的 redis 实例如何支撑千万级左右的延迟任务量？](https://www.infoq.cn/article/97duwvxvbe20aohsurrr)
    * [基于redis实现的轻量级延迟队列](https://mp.weixin.qq.com/s?__biz=MzUxOTc4NjEyMw==&mid=2247582766&idx=3&sn=5f17a7e5f5f7d69c830c70860a308e0f&chksm=f806a721092d7827a40da7bd5198fa18ec19264753edff50cc7fab4b5b5036499c1d52190330&mpshare=1&scene=1&srcid=0808oStXNbqTLNWPMaA9TtCT&sharer_shareinfo=4e985c129a5c00563bc3ea7754ddbf05&sharer_shareinfo_first=5093a797e556e675f530afb3b26a09e4&version=4.1.10.99312&platform=mac#rd)
