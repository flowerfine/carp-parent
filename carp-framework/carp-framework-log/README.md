# Log Framework

日志框架。

支持日志场景：

* [carp-framework-log-web](./carp-framework-log-web)。接口请求日志 + 用户行为日志。需配合 [carp-framework-web](../carp-framework-web) 使用
* 数据变动日志。未提供通用实现，只提供解决思路
  * 解决思路
    * 获取数据对象变动信息。变动信息可采用类似 mysql binlog 形式如 add、update、delete + update before + update after
    * 存储对象变动信息。应用收缩数据**写（增、删、改）场景**，在增、删、改场景记录对象的变动日志。对于改场景，需比较对象内字段的变动信息，如姓名字段修改（修改前 -> 修改后），自我介绍字段新增，所在城市字段删除

  * 实现方案
    * 获取数据对象变动信息。
      * 增场景。记录新增
      * 删场景。记录删除
      * 改场景。对对象进行 DIFF，记录对象字段的增、删、改场景。DIFF 方案：[diff](https://kalencaya.github.io/docs/project/java/diff.html)

    * 存储对象变动信息。

* [carp-framework-log-realtime](./carp-framework-log-realtime)。实时任务日志。如调度系统、任务系统、Workflow 系统、CI/CD 系统等都存在实时输出日志输出，支持前端实时滚动输出需求，类似的还有 Flink 数据实时查询场景。
