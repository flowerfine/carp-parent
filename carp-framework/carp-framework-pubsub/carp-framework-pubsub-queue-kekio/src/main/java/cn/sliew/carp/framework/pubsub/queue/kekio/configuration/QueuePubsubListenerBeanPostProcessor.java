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

import cn.sliew.carp.framework.pubsub.annotation.PubsubListener;
import cn.sliew.carp.framework.queue.kekio.MessageHandler;
import cn.sliew.carp.framework.queue.kekio.pubsub.MessageListener;
import cn.sliew.carp.framework.queue.kekio.pubsub.PubsubChannelFactory;
import cn.sliew.carp.framework.queue.kekio.pubsub.QueuePubsubSubscriber;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class QueuePubsubListenerBeanPostProcessor
        implements ApplicationContextAware, BeanPostProcessor, InitializingBean {

    private ApplicationContext applicationContext;
    private PubsubChannelFactory pubsubChannelFactory;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.pubsubChannelFactory = applicationContext.getBean(PubsubChannelFactory.class);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (MessageHandler.class.isAssignableFrom(targetClass)) {
            PubsubListener ann = targetClass.getAnnotation(PubsubListener.class);
            if (ann != null) {
                String queue = ann.queue();
                String group = ann.group();
                pubsubChannelFactory.get(queue).register(new QueuePubsubSubscriber(group, (MessageHandler) bean));
            }
        }
        return bean;
    }
}
