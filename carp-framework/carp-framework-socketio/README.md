# SocketIO Framework

集成了 [Socket.IO](https://socket.io/zh-CN/)，依赖 [netty-socketio](https://github.com/mrniko/netty-socketio)：

* 提供了开箱即用的服务端 Socket.IO 集成
* 支持注解式开发。通过 `@Component` 定义的类，添加 `@OnConnect`、`@OnDisconnect`、`@OnEvent` 注解
* 命名空间支持。可通过 `CarpSocketIoNamespace` 取代 `@Component` 注解，添加 namespace
* 常用方法默认实现。提供 `CarpConnectionListener` 和 `SocketIOConnectionManager` 快速处理 client 连接和中断
  * `CarpConnectionListener` 默认使用 connection 的 sessionId 作为用户 id。
  * 使用 `CarpConnectionListener` 时建议先处理 connection 鉴权以获取用户信息。
* 广播消息增强。服务端集群部署下通过广播消息实现消息推送

## 使用方式

* 配置文件
  * 增加 `carp.framework.socketio` 配置。具体查看 `SocketIOAutoConfiguration`
* 添加 `@EnableSocketIO` 注解

## 集群解决方案

服务端集群部署情况下，client 与其中一台 server 建立连接。当另外一台 server 向 client 推送消息时，因为没有与 client 的连接推送失败。

解决方案是将 server 欲推送的消息通过广播方式，广播至所有 server，与 client 建立连接的 server 收到广播消息，将消息推送到 client。

广播实现方式有 2 种：

* 通过 pub-sub 机制自建广播
* netto-socketio 内置广播操作

### pub-sub 机制

通过 消息队列、redis 等工具，发送广播消息至集群所有集群，集群接收广播消息，检测是否有对应的 client 连接。如果有则推送消息至 client。

如 [sreworks](https://github.com/alibaba/SREWorks/) :

* [WebSocketWorkflowLogController](https://github.com/alibaba/SREWorks/blob/main/paas/appmanager/tesla-appmanager-workflow/src/main/java/com/alibaba/tesla/appmanager/workflow/controller/WebSocketWorkflowLogController.java)
* [WorkflowDeployHandler](https://github.com/alibaba/SREWorks/blob/main/paas/appmanager/tesla-appmanager-server/src/main/resources/dynamicscripts/WorkflowDeployHandler.groovy)
* [StreamLogServiceImpl](https://github1s.com/alibaba/SREWorks/blob/main/paas/appmanager/tesla-appmanager-common-service/src/main/java/com/alibaba/tesla/appmanager/common/service/impl/StreamLogServiceImpl.java#L31)

```java
package com.alibaba.tesla.appmanager.workflow.controller;

...
import com.alibaba.tesla.appmanager.server.storage.Storage;
...
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@Component
@ServerEndpoint("/ws/workflow/{taskId}/ws-logs")
public class WebSocketWorkflowLogController {

    private static WorkflowTaskProvider workflowTaskProvider;

    private static StreamMessageListenerContainer streamMessageListenerContainer;

    private static Storage storage;

    @OnOpen
    public void onOpen(@PathParam("taskId") Long taskId, Session session) throws IOException {
        log.info("new ws connection: {}", taskId);

        WorkflowTaskDTO response = workflowTaskProvider.get(taskId, true);
        String streamKey = String.format("%s_%s", RedisKeyConstant.WORKFLOW_TASK_LOG, response.getId());
        if (...) {
          	// 执行中，获取 redis 推送的日志
            streamMessageListenerContainer.receive(StreamOffset.fromStart(streamKey),
                    message -> {
                        Object stream = message.getStream();
                        RecordId id = message.getId();
                        Map<String, String> messageValue = (Map) message.getValue();
                        log.debug("receive a message. stream: [{}],id: [{}],value: [{}]", stream, id, messageValue);
                        try {
                            session.getBasicRemote().sendText(messageValue.get(STREAM_LOG_KEY));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } else {
          	// 已结束，获取存储的日志文件内容
            String date = new SimpleDateFormat("yyyyMMdd").format(response.getGmtCreate());
            String bucketName = ...
            String objectName = String.format("stream_log/%s/%s.txt", date, streamKey);
            session.getBasicRemote().sendText(storage.getObjectContent(bucketName,objectName));
        }
    }
}
```

```groovy

@Slf4j
class WorkflowDeployHandler implements WorkflowHandler {

    @Autowired
    private StreamLogService streamLogService

    @Override
    ExecuteWorkflowHandlerRes execute(ExecuteWorkflowHandlerReq request) throws InterruptedException {
        def streamKey = String.format("%s_%s", RedisKeyConstant.WORKFLOW_TASK_LOG, request.getTaskId())
        try {
            streamLogService.info(streamKey, "start execute WorkflowDeployHandler")
            def configuration = request.getConfiguration()
            def context = request.getContext()
            def policies = request.getTaskProperties().getJSONArray("policies")
            if (policies != null && policies.size() > 0) {
                for (def policyName : policies.toJavaList(String.class)) {

                    ...

                    streamLogService.info(streamKey, String.format("preapre to execute policy in workflow task|" +
                            "workflowInstanceId=%s|workflowTaskId=%s|" +
                            "appId=%s|context=%s|configuration=%s", request.getInstanceId(), request.getTaskId(),
                            request.getAppId(), JSONObject.toJSONString(context), JSONObject.toJSONString(configuration)), log)
                    
                    ...

                    log.info("policy has exeucted in workflow task|workflowInstanceId={}|workflowTaskId={}|appId={}|" +
                            "context={}|configuration={}", request.getInstanceId(), request.getTaskId(), request.getAppId(),
                            JSONObject.toJSONString(context), JSONObject.toJSONString(configuration))
                    streamLogService.info(streamKey, "policy has exeucted in workflow task")
                }
            }
            def deployAppId = WorkflowHandlerUtil.deploy(configuration, null, request.getCreator())
            log.info("deploy request has applied|workflowInstanceId={}|workflowTaskId={}|appId={}|context={}|" +
                    "configuration={}", request.getInstanceId(), request.getTaskId(), request.getAppId(),
                    JSONObject.toJSONString(context), JSONObject.toJSONString(configuration))
            streamLogService.info(streamKey, "deploy request has applied")
            
            ...
            
        } catch (Exception ex) {
            streamLogService.info(streamKey, ExceptionUtils.getStackTrace(ex))
            throw ex;
        } finally {
            streamLogService.info(streamKey, "execute WorkflowDeployHandler finish!")
            streamLogService.clean(streamKey, true);
        }
    }
}
```

### netty-socketio 广播

* 集成 redisson 或 hazelcast。
* 发送广播消息

```java
SocketIOServer server = ...;
SocketIONamespace namespace = server.addNamespace("/test");

// 发送广播消息
namespace.getBroadcastOperations().sendEvent(eventKey, data);

// 处理广播消息
namespace.getClient(sessionId).sendEvent(eventKey, data);
```

