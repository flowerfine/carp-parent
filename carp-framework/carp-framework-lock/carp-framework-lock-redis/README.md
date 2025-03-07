# Lock Framework Redis

集成了基于 redisson 的分布式锁实现，使用时：

* 配置文件
  * 增加 `spring.datasource.redis` 配置
  * 增加 `carp.framework.lock.redis.enabled: true` 配置
* 导入 `RedisLockAndRunAutoConfiguration` 配置类

redisson 的分布式锁使用的是 [Lock](https://redisson.org/docs/data-and-services/locks-and-synchronizers/#lock)。Lock 未设置 `leaseTime`，默认锁过期时间是 `30s`，redisson 会自动对锁进行续期，直到手动对释放锁。灵活应对任意时长的任务实现
