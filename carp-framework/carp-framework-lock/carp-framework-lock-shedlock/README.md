# Lock Framework ShedLock

集成了基于 spring 的 `JdbcTemplate` 实现，使用时：

* 数据库中创建名为 `shedlock` 的表
* 配置文件中增加 `spring.datasource.shedlock` 数据源配置
* 导入 `ShedLockExecutorAutoConfiguration` 配置类