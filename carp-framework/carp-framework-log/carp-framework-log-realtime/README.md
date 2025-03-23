# Log Framework Realtime

实时日志

实现思路：

* 服务端存储日志
  * 消息队列或 Redis。日志输出任务将日志写入消息队列或 Redis 中，服务端甚至客户端消费日志，实时展示日志。消息队列中消息无法主动查询、只能被动消费，消息队列或 Redis 也无法长时间存储日志。如需在任务结束后继续查看日志数据，需增加日志归档功能，将消息队列或 Redis 中日志数据存储到文件系统中供日后查询。
  * 文件存储。以追求方式将日志写入文件系统中。参考：[xxl-job#XxlJobFileAppender](https://github.com/xuxueli/xxl-job/blob/master/xxl-job-core/src/main/java/com/xxl/job/core/log/XxlJobFileAppender.java#L89)
* 前端查询日志
  * 主动推送。前端与服务端建立连接，服务端通过 websocket、sse 或 Socket.IO 等方式实时推送日志。
  * 被动查询。前端通过服务端提供的接口，获取日志。参考：[xxl-job#JobLogController](https://github.com/xuxueli/xxl-job/blob/master/xxl-job-admin/src/main/java/com/xxl/job/admin/controller/JobLogController.java#L144)、[xxljob#XxlJobFileAppender](https://github.com/xuxueli/xxl-job/blob/master/xxl-job-core/src/main/java/com/xxl/job/core/log/XxlJobFileAppender.java#L138)
* 前端 + 服务端
  * 主动推送。
    * 消息队列或 Redis。服务端监听消息队列或 Redis，将监听到的消息推送至服务端。前端和服务端建立连接时，需检测任务是否结束，如已结束可直接读取归档日志
    * 文件存储。服务端监听日志或轮询日志文件，将新追加的日志推送至服务端
  * 被动查询
    * 消息队列或 Redis。前端携带消费 offset 信息，服务端根据 offset 消费数据，收集到数据后将最新的 offset 和数据一起返回至前端
    * 文件存储。前端携带文件读取位置，服务端从指定读取文件开始读取文件内容，将读取到的数据和最新的读取位置一起返回至前端

解决方案：

* 服务端将日志推送至 Redis Stream 中。当任务结束，服务端读取 Redis Stream 中所有数据存入文件系统
* 前端与服务端建立连接。前端发送任务信息，服务端推送任务日志。服务端推送前先判断任务是否结束：
  * 进行中。服务端从头消费 Redis Stream 中数据，推送前端
  * 已结束。服务端读取文件系统中数据，推送前端



