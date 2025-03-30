/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.sliew.carp.framework.pubsub.queue.kekio.configuration;

import cn.hutool.extra.spring.SpringUtil;
import cn.sliew.carp.framework.pubsub.annotation.PubsubListener;
import cn.sliew.carp.framework.pubsub.model.PubsubChannelFactory;
import cn.sliew.carp.framework.pubsub.queue.kekio.QueuePubsubSubscriber;
import cn.sliew.carp.framework.queue.kekio.MessageHandler;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class QueuePubsubListenerBootstrap implements ApplicationRunner {

    @Autowired
    private PubsubChannelFactory pubsubChannelFactory;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String, Object> beansWithAnnotation = SpringUtil.getApplicationContext().getBeansWithAnnotation(PubsubListener.class);
        for (Map.Entry<String, Object> entry : beansWithAnnotation.entrySet()) {
            Object bean = entry.getValue();
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            if (MessageHandler.class.isAssignableFrom(targetClass)) {
                PubsubListener ann = targetClass.getAnnotation(PubsubListener.class);
                if (ann != null) {
                    String queue = ann.queue();
                    String group = ann.group();
                    pubsubChannelFactory.get(queue).register(new QueuePubsubSubscriber(group, (MessageHandler) bean));
                }
            }
        }
    }
}
