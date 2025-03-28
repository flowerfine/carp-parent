# Queue Frameowrk Kekio

[spinnaker](https://github.com/spinnaker) 中 [orca](https://github.com/spinnaker/orca) 服务的 kekio 延迟队列。orca 相关依赖使用 springboot 2.7x，与 carp 使用的 springboot 3.3x 依赖存在兼容问题，无法直接加入 kekio 依赖至 pom.xml 中使用，需迁移过来。

kekio 提供了 3 种实现：

* memory。基于 JDK DelayQueue 实现。
* redis。基于 [jedis](https://github.com/redis/jedis) 实现，支持 standalone 和 redis-cluster 2 种模式。
* jdbc。支持 MySQL 和 PostgreSQL

本模块支持 memory 和 redis 2 种实现，不支持 jdbc

## 使用指南

